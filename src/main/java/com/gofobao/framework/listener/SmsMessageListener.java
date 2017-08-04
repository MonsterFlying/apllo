package com.gofobao.framework.listener;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
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

/**
 * Created by Max on 17/5/17.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_SMS)
public class SmsMessageListener{

    @Autowired
    CommonSmsProvider commonSmsProvider ;

    static Gson GSON = new GsonBuilder().create() ;

    @RabbitHandler
    public void process(String message) {
        log.info(String.format("smsMessageListener process detail: %s", message));
        try {
            Preconditions.checkNotNull(message, "SmsMessageListener process message is empty") ;
            Map<String, Object> body = GSON.fromJson(message, TypeTokenContants.MAP_TOKEN);
            Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "SmsMessageListener process tag is empty ") ;
            Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "SmsMessageListener process body is empty ") ;
            String tag = body.get(MqConfig.MSG_TAG).toString();
            Map<String, String> msg = (Map<String, String>)body.get(MqConfig.MSG_BODY) ;
            boolean result;
            if (tag.equals(MqTagEnum.SMS_RECEIVED_REPAY.getValue())){
                result = commonSmsProvider.doSmsNoticeByReceivedRepay(tag,msg);
            }else if(tag.equals(MqTagEnum.SMS_WINDMILL_USER_REGISTER.getValue())){
                result = commonSmsProvider.doSmsWindmillRegister(tag, msg);
            }else {
                result = commonSmsProvider.doSendMessageCode(tag, msg);
            }
            if(!result){
                log.error(String.format("SmsMessageListener process process error: %s", message));
            }
        }catch (Throwable e){
            log.error("SmsMessageListener process do exception", e);
        }
    }


}
