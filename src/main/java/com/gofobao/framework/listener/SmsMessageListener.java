package com.gofobao.framework.listener;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.listener.providers.CommonSmsProvider;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.gofobao.framework.common.rabbitmq.MqTagEnum.*;

/**
 * Created by Max on 17/5/17.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_SMS)
public class SmsMessageListener{

    @Autowired
    CommonSmsProvider commonSmsProvider ;

    Gson gson = new GsonBuilder().create() ;

    @RabbitHandler
    public void process(String message) {
        Preconditions.checkNotNull(message, "SmsMessageListener process message is empty") ;
        Map<String, Object> body = gson.fromJson(message, TypeTokenContants.MAP_TOKEN);


        Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "SmsMessageListener process tag is empty ") ;
        Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "SmsMessageListener process body is empty ") ;
        String tag = body.get(MqConfig.MSG_TAG).toString();
        Map<String, String> msg = (Map<String, String>)body.get(MqConfig.MSG_BODY) ;

        boolean result = false;
        if((tag.equals(SMS_REGISTER.getValue()))
                || (tag.equals(SMS_RESET_PASSWORD.getValue()))
                || (tag.equals(SMS_SWICTH_PHONE.getValue()))){
            result  = commonSmsProvider.doSendMessageCode(tag, msg);
        }

        if(!result){
            log.error(String.format("SmsMessageListener process process error: %s", message));
        }

    }


}
