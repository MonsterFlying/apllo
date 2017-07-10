package com.gofobao.framework.listener.providers;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
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
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Created by Zeke on 2017/7/10.
 */
@Component
@Slf4j
public class CreditProvider {

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

    public boolean endThirdCredit(Map<String, String> msg) throws Exception{
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

            Optional<List<Tender>> tenderOpetional = Optional.of(tenderList);
            tenderOpetional.ifPresent(tenders -> tenderList.forEach(tender -> {
                UserThirdAccount tenderUserThirdAccount = userThirdAccountService.findByUserId(tender.getUserId());

                CreditEndReq creditEndReq = new CreditEndReq();
                creditEndReq.setAccountId(borrowUserThirdAccount.getAccountId());
                creditEndReq.setChannel(ChannelContant.HTML);
                creditEndReq.setForAccountId(tenderUserThirdAccount.getAccountId());
                creditEndReq.setAuthCode(tender.getAuthCode());
                creditEndReq.setOrderId(JixinHelper.getOrderId(JixinHelper.END_CREDIT_PREFIX));
                creditEndReq.setProductId(borrow.getProductId());
                CreditEndResp creditEndResp = jixinManager.send(JixinTxCodeEnum.CREDIT_END, creditEndReq, CreditEndResp.class);
                if ((ObjectUtils.isEmpty(creditEndResp)) || (!JixinResultContants.SUCCESS.equals(creditEndResp.getRetCode()))) {
                    String massage = ObjectUtils.isEmpty(creditEndResp) ? "当前网络不稳定，请稍候重试" : creditEndResp.getRetMsg();
                    try {
                        throw new Exception(massage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));

        } while (false);
        return false;
    }
}
