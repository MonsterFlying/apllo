package com.gofobao.framework.listener.providers;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.batch_credit_end.BatchCreditEndReq;
import com.gofobao.framework.api.model.batch_credit_end.BatchCreditEndResp;
import com.gofobao.framework.api.model.batch_credit_end.CreditEnd;
import com.gofobao.framework.api.model.credit_end.CreditEndReq;
import com.gofobao.framework.api.model.credit_end.CreditEndResp;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.JixinHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.system.contants.ThirdBatchLogContants;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.service.ThirdBatchLogService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Created by Zeke on 2017/7/10.
 */
@Component
@Slf4j
public class CreditProvider {

    final Gson gson = new GsonBuilder().create();

    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private TenderService tenderService;
    @Autowired
    private UserThirdAccountService userThirdAccountService;
    @Autowired
    private JixinManager jixinManager;
    @Autowired
    private JixinHelper jixinHelper;
    @Autowired
    private ThirdBatchLogService thirdBatchLogService;

    public boolean endThirdCredit(Map<String, String> msg) throws Exception {
        do {
            Long borrowId = NumberHelper.toLong(StringHelper.toString(msg.get(MqConfig.MSG_BORROW_ID)));
            Borrow borrow = borrowService.findById(borrowId);
            Preconditions.checkNotNull(borrow, "creditProvider endThirdCredit: 借款不能为空!");

            Specification<BorrowRepayment> brs = Specifications
                    .<BorrowRepayment>and()
                    .eq("borrowId", borrowId)
                    .eq("status", 0)
                    .build();
            long count = borrowRepaymentService.count(brs);
            if (count > 0) {
                log.info("creditProvider endThirdCredit: 存在未还清还款！");
                break;
            }

            Specification<Tender> ts = Specifications
                    .<Tender>and()
                    .eq("borrowId", borrowId)
                    .eq("status", 1)
                    .build();
            List<Tender> tenderList = tenderService.findList(ts);
            if (CollectionUtils.isEmpty(tenderList)) {
                log.info("creditProvider endThirdCredit: 未找到投递成功债权！");
                break;
            }

            UserThirdAccount borrowUserThirdAccount = userThirdAccountService.findByUserId(borrow.getUserId());
            Preconditions.checkNotNull(borrow, "creditProvider endThirdCredit: 借款不能为空!");

            UserThirdAccount tenderUserThirdAccount = null;
            List<CreditEnd> creditEndList = new ArrayList<>();
            CreditEnd creditEnd = null;
            String orderId = null;
            for (Tender tender : tenderList) {
                creditEnd = new CreditEnd();
                orderId =  JixinHelper.getOrderId(JixinHelper.END_CREDIT_PREFIX);

                tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());
                creditEnd.setAccountId(borrowUserThirdAccount.getAccountId());
                creditEnd.setOrderId(orderId);
                creditEnd.setAuthCode(tender.getAuthCode());
                creditEnd.setForAccountId(tenderUserThirdAccount.getAccountId());
                creditEnd.setProductId(borrow.getProductId());
                creditEndList.add(creditEnd);

                tender.setThirdCreditEndOrderId(orderId);
            }

            tenderService.save(tenderList);

            //发送批次结束债权
            Date nowDate = new Date();
            String batchNo = jixinHelper.getBatchNo();

            BatchCreditEndReq request = new BatchCreditEndReq();
            request.setBatchNo(batchNo);
            request.setTxCounts(String.valueOf(creditEndList.size()));
            request.setNotifyURL("");
            request.setRetNotifyURL("");
            request.setAcqRes(String.valueOf(borrowId));
            request.setSubPacks(gson.toJson(creditEndList));

            BatchCreditEndResp creditEndResp = jixinManager.send(JixinTxCodeEnum.BATCH_CREDIT_END, request, BatchCreditEndResp.class);
            if ((ObjectUtils.isEmpty(creditEndResp)) || (!JixinResultContants.BATCH_SUCCESS.equalsIgnoreCase(creditEndResp.getReceived()))) {
                throw new Exception(creditEndResp.getRetMsg());
            }

            //记录日志
            ThirdBatchLog thirdBatchLog = new ThirdBatchLog();
            thirdBatchLog.setBatchNo(batchNo);
            thirdBatchLog.setCreateAt(nowDate);
            thirdBatchLog.setUpdateAt(nowDate);
            thirdBatchLog.setSourceId(borrowId);
            thirdBatchLog.setType(ThirdBatchLogContants.BATCH_CREDIT_END);
            thirdBatchLog.setRemark("即信批次还款");
            thirdBatchLogService.save(thirdBatchLog);

        } while (false);
        return false;
    }
}
