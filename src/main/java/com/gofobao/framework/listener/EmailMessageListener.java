package com.gofobao.framework.listener;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.listener.providers.CommonEmaiProvider;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.gofobao.framework.common.rabbitmq.MqTagEnum.SEND_BORROW_PROTOCOL_EMAIL;
import static com.gofobao.framework.common.rabbitmq.MqTagEnum.SMS_EMAIL_BIND;

/**
 * Created by Max on 17/5/17.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_EMAIL)
public class EmailMessageListener {

    @Autowired
    CommonEmaiProvider commonEmaiProvider;

    static Gson GSON = new GsonBuilder().create();

    @RabbitHandler
    public void process(String message) {
        log.info(String.format("EmailMessageListener process detail: %s", message));
        try {
            Preconditions.checkNotNull(message, "EmailMessageListener process message is empty");
            Map<String, Object> body = GSON.fromJson(message, TypeTokenContants.MAP_TOKEN);
            Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "EmailMessageListener process tag is empty ");
            Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "EmailMessageListener process body is empty ");
            String tag = body.get(MqConfig.MSG_TAG).toString();
            Map<String, String> msg = (Map<String, String>) body.get(MqConfig.MSG_BODY);
            boolean result = false;
            if (tag.equals(SMS_EMAIL_BIND.getValue())) {  // 发送邮箱
                result = commonEmaiProvider.doSendMessageCode(tag, msg);
            } else if (tag.equals(SEND_BORROW_PROTOCOL_EMAIL.getValue())) {
                result = commonEmaiProvider.doSendBorrowProtocolEmail(tag, msg);
            }

            if (!result) {
                log.error("EmailMessageListener process process error ");
            }

        } catch (Throwable e) {
            log.error("EmailMessageListener process do exception", e);
        }
    }
}
