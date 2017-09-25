package com.gofobao.framework.listener;

import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.JacksonHelper;
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
            Map<String, Object> msg = (Map<String, Object>) body.get(MqConfig.MSG_BODY);
            String tag = body.get(MqConfig.MSG_TAG).toString();
            //理财计划满额时通知后台处理
            if (tag.equals(MqTagEnum.FINANCE_PLAN_FULL_NOTIFY)) {
                financePlanProvider.pullScaleNotify(msg);
            }
        } catch (Exception e) {

        }

    }

}
