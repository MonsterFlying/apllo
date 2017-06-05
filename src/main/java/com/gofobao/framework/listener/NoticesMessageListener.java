package com.gofobao.framework.listener;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.listener.providers.NoticesMessageProvider;
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
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_NOTICE)
public class NoticesMessageListener {
    @Autowired
    NoticesMessageProvider noticeMessageProvider ;

    static Gson GSON = new GsonBuilder().create() ;

    @RabbitHandler
    public void process(String message) {
        log.info(String.format("NoticMessageListener process info: %s", message));
        try {
            Preconditions.checkNotNull(message, "NoticMessageListener process message is empty") ;
            Map<String, Object> body = GSON.fromJson(message, TypeTokenContants.MAP_TOKEN);
            Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "NoticMessageListener process tag is empty ") ;
            Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "NoticMessageListener process body is empty ") ;
            String tag = body.get(MqConfig.MSG_TAG).toString();
            Map<String, String> msg = (Map<String, String>)body.get(MqConfig.MSG_BODY) ;
            boolean result  = noticeMessageProvider.addNoticeMessage(tag, msg);
            if(!result){
                log.error(String.format("NoticMessageListener process process error: %s", message));
            }
        }catch (Throwable e){
            log.error("NoticMessageListener process do exception", e);
        }


    }

}
