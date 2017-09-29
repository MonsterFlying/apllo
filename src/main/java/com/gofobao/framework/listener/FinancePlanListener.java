package com.gofobao.framework.listener;

import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.JacksonHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.listener.providers.FinancePlanProvider;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Zeke on 2017/8/14.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_FINANCE_PLAN)
public class FinancePlanListener {

    @Autowired
    private FinancePlanProvider financePlanProvider;


    @RabbitHandler
    public void process(String message) {

        log.info(String.format("RedPackageListener process detail: %s", message));
        Preconditions.checkNotNull(message, "RedPackageListener process message is empty");
        try {
            Map<String, Object> body = JacksonHelper.json2map(message);
            Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG));
            Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY));
            Map<String, String> msg = (Map<String, String>) body.get(MqConfig.MSG_BODY);
            String tag = body.get(MqConfig.MSG_TAG).toString();
            Long transferId = NumberHelper.toLong(StringHelper.toString(msg.get(MqConfig.MSG_TRANSFER_ID)));
            //理财计划满额时通知后台处理
            if (tag.equals(MqTagEnum.FINANCE_PLAN_FULL_NOTIFY.getValue())) {
                financePlanProvider.pullScaleNotify(msg);
            } else if (tag.equals(MqTagEnum.AGAIN_VERIFY_FINANCE_TRANSFER.getValue())) {  // 理财计划债权转让复审
                try {
                    financePlanProvider.againVerifyFinanceTransfer(msg);
                    log.info("===========================AutoTenderListener===========================");
                    log.info("理财计划债权转让复审成功! transferId：" + transferId);
                    log.info("========================================================================");
                } catch (Throwable throwable) {
                    log.error("理财计划债权转让复审异常:", throwable);
                }
            }
        } catch (Exception e) {
            log.error("理财计划复审异常:", e);
        }

    }

}
