package com.gofobao.framework.listener;

import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.vo.request.VoRepayAll;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoAdvanceReq;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Zeke on 2017/7/21.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_REPAYMENT)
public class RepaymentListener {

    final Gson gson = new GsonBuilder().create();

    @Autowired
    private BorrowBiz borrowBiz;
    @Autowired
    private RepaymentBiz repaymentBiz;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;

    @RabbitHandler
    private void process(String message) {
        Preconditions.checkNotNull(message, "AutoTenderListener process message is empty");
        Map<String, Object> body = gson.fromJson(message, TypeTokenContants.MAP_TOKEN);
        Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "AutoTenderListener process tag is empty ");
        Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "AutoTenderListener process body is empty ");
        String tag = body.get(MqConfig.MSG_TAG).toString();
        Map<String, String> msg = (Map<String, String>) body.get(MqConfig.MSG_BODY);

        String borrowId = StringHelper.toString(msg.get(MqConfig.MSG_BORROW_ID));
        String repaymentId = StringHelper.toString(msg.get(MqConfig.MSG_REPAYMENT_ID));
        if (tag.equals(MqTagEnum.REPAY_ALL.getValue())) {  // 自动投标
            if (!StringUtils.isEmpty(borrowId)) {
                //=========================================
                // 提前结清
                //=========================================
                VoRepayAll voRepayAll = new VoRepayAll();
                voRepayAll.setBorrowId(NumberHelper.toLong(borrowId));
                borrowBiz.repayAll(voRepayAll);
            }
        } else if (tag.equals(MqTagEnum.REPAY_ADVANCE.getValue())) {
            if (!StringUtils.isEmpty(repaymentId)) {
                BorrowRepayment borrowRepayment = borrowRepaymentService.findById(NumberHelper.toLong(repaymentId));
                VoRepayReq voRepayReq = new VoRepayReq();
                voRepayReq.setRepaymentId(borrowRepayment.getId());
                voRepayReq.setUserId(borrowRepayment.getUserId());
                voRepayReq.setInterestPercent(null);
                voRepayReq.setIsUserOpen(true);
                try {
                    repaymentBiz.thirdBatchRepayBail(voRepayReq);
                } catch (Exception e) {
                    log.error("RepaymentListener process error:", e);
                }
            }
        } else if (tag.equals(MqTagEnum.ADVANCE.getValue())) {
            if (!StringUtils.isEmpty(repaymentId)) {
                try {
                    VoAdvanceReq voAdvanceReq = new VoAdvanceReq();
                    voAdvanceReq.setRepaymentId(NumberHelper.toLong(repaymentId));
                    repaymentBiz.advance(voAdvanceReq);
                } catch (Exception e) {
                    log.error("RepaymentListener process error:", e);
                }
            }
        } else if (tag.equals(MqTagEnum.REPAY.getValue())) {
            if (!StringUtils.isEmpty(repaymentId)){
                try {
                    BorrowRepayment borrowRepayment = borrowRepaymentService.findById(NumberHelper.toLong(repaymentId));
                    VoRepayReq voRepayReq = new VoRepayReq();
                    voRepayReq.setRepaymentId(borrowRepayment.getId());
                    voRepayReq.setUserId(borrowRepayment.getUserId());
                    voRepayReq.setInterestPercent(null);
                    repaymentBiz.repay(voRepayReq);
                } catch (Exception e) {
                    log.error("RepaymentListener process error:", e);
                }
            }
        } else {
            log.error("AutoTenderListener 未找到对应的type");
        }
    }
}
