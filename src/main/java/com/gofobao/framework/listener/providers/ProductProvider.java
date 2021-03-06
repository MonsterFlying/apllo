package com.gofobao.framework.listener.providers;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelReq;
import com.gofobao.framework.api.model.batch_cancel.BatchCancelResp;
import com.gofobao.framework.api.model.batch_credit_end.BatchCreditEndReq;
import com.gofobao.framework.api.model.batch_credit_end.BatchCreditEndResp;
import com.gofobao.framework.api.model.batch_credit_end.CreditEnd;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.finance.biz.FinancePlanBiz;
import com.gofobao.framework.finance.biz.impl.FinancePlanBizImpl;
import com.gofobao.framework.finance.entity.FinancePlan;
import com.gofobao.framework.finance.service.FinancePlanService;
import com.gofobao.framework.finance.vo.request.VoTenderFinancePlan;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.product.entity.ProductOrder;
import com.gofobao.framework.product.entity.ProductOrderBuyLog;
import com.gofobao.framework.product.entity.ProductPlan;
import com.gofobao.framework.product.service.ProductOrderBuyLogService;
import com.gofobao.framework.product.service.ProductOrderService;
import com.gofobao.framework.product.service.ProductPlanService;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.service.TenderService;
import com.gofobao.framework.tender.service.TransferBuyLogService;
import com.gofobao.framework.tender.service.TransferService;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Created by Zeke on 2017/7/10.
 */
@Component
@Slf4j
public class ProductProvider {

    @Autowired
    ProductOrderService productOrderService;
    @Autowired
    ProductPlanService productPlanService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private FinancePlanService financePlanService;
    @Autowired
    private FinancePlanBiz financePlanBiz;
    @Autowired
    private ProductOrderBuyLogService productOrderBuyLogService;

    final Gson gson = new GsonBuilder().create();

    /**
     * @param msg
     * @return
     * @throws Exception 生成广富送计划启动
     */
    public boolean generateProductPlan(Map<String, String> msg) throws Exception {
        log.info(String.format("生成广富送计划启动: %s", gson.toJson(msg)));
        Date nowDate = new Date();
        /*订单编号*/
        String orderNumber = msg.get(MqConfig.MSG_ORDER_NUMBER);
        /*广富送订单记录*/
        ProductOrder productOrder = productOrderService.findByOrderNumberLock(orderNumber);
        /*广富送商品计划*/
        Specification<ProductOrderBuyLog> pobls = Specifications
                .<ProductOrderBuyLog>and()
                .eq("productOrderId", productOrder.getId())
                .build();
        List<ProductOrderBuyLog> productOrderBuyLogList = productOrderBuyLogService.findList(pobls);
        /*计划id集合*/
        Set<Long> planIds = productOrderBuyLogList.stream().map(ProductOrderBuyLog::getPlanId).collect(Collectors.toSet());
        Specification<ProductPlan> pps = Specifications
                .<ProductPlan>and()
                .in("id", planIds.toArray())
                .build();
        List<ProductPlan> productPlanList = productPlanService.findList(pps);
        Map<Long, ProductPlan> productPlanMap = productPlanList.stream().collect(Collectors.toMap(ProductPlan::getId, Function.identity()));
        for (ProductOrderBuyLog productOrderBuyLog : productOrderBuyLogList) {
            ProductPlan productPlan = productPlanMap.get(productOrderBuyLog.getPlanId());
            //生成理财计划
            String sql = "SELECT count(1) FROM gfb_finance_plan WHERE date(created_at) = '%s' and time_limit = %s";
            //周期
            int period = jdbcTemplate.queryForObject(String.format(sql, DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMD), productPlan.getTimeLimit()), Integer.class);
            period++;
            FinancePlan financePlan = new FinancePlan();
            financePlan.setName(String.format("优质车贷标-%s-%s月-%s", DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMD_NUM), productPlan.getTimeLimit(), period));
            financePlan.setStatus(0);
            financePlan.setBaseApr(productPlan.getApr());
            financePlan.setMoney(productOrder.getPayMoney());
            financePlan.setCreateId(productOrder.getUserId());
            financePlan.setUpdateId(productOrder.getUserId());
            financePlan.setLockPeriod(productPlan.getTimeLimit());
            financePlan.setLowest(productOrder.getPayMoney());
            financePlan.setType(2);
            financePlan.setStatus(1);
            financePlan.setLowestShow(productOrder.getPayMoney());
            financePlan.setLeftMoney(0L);
            financePlan.setRightMoney(0L);
            financePlan.setEndLockAt(getFinancePlanEndLockAt(productPlan.getTimeLimit()));
            financePlan.setTimeLimit(productPlan.getTimeLimit());
            financePlan.setMoneyYes(0L);
            financePlan.setOrderNumber(orderNumber);
            financePlan.setFinishedState(false);
            financePlan.setSubPointCount(0);
            financePlan.setTotalSubPoint(0);
            financePlan.setMost(0L);
            financePlan.setAppendMultipleAmount(0);
            financePlan.setDescription("");
            financePlan.setContractNo(String.format("LCJH%s", DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMD_NUM)));
            financePlan = financePlanService.save(financePlan);
            financePlan.setContractNo(String.format("LCJH%s%s", DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMD_NUM), financePlan.getId()));
            financePlan.setCreatedAt(nowDate);
            financePlan.setUpdatedAt(nowDate);
            financePlanService.save(financePlan);
            //生成理财计划购买记录
            VoTenderFinancePlan voTenderFinancePlan = new VoTenderFinancePlan();
            voTenderFinancePlan.setUserId(productOrder.getUserId());
            voTenderFinancePlan.setFinancePlanId(financePlan.getId());
            voTenderFinancePlan.setMoney(MoneyHelper.divide(productOrder.getPayMoney(), 100));
            voTenderFinancePlan.setRemark("商品计划");
            financePlanBiz.tenderFinancePlan(voTenderFinancePlan);
        }
        return true;
    }

    /**
     * 获取理财计划退出日期
     *
     * @param timeLimit
     */
    public Date getFinancePlanEndLockAt(int timeLimit) {
        Date tomorrow = DateHelper.addDays(new Date(), 1);
        Date endLockAt = DateHelper.addMonths(tomorrow, timeLimit);
        if ((DateHelper.getMonth(tomorrow) + timeLimit) % 12 != DateHelper.getMonth(endLockAt) % 12) {
            endLockAt = DateHelper.subDays(DateHelper.setDays(endLockAt, 1), 1);
        }
        return endLockAt;
    }

}
