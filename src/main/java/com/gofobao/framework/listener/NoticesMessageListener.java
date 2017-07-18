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

import static com.gofobao.framework.common.rabbitmq.MqTagEnum.NOTICE_PUBLISH;

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
        log.info(String.format("NoticesMessageListener process detail: %s", message));
        try {
            Preconditions.checkNotNull(message, "NoticesMessageListener process message is empty") ;
            Map<String, Object> body = GSON.fromJson(message, TypeTokenContants.MAP_TOKEN);
            Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "NoticesMessageListener process tag is empty ") ;
            Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "NoticesMessageListener process body is empty ") ;
            String tag = body.get(MqConfig.MSG_TAG).toString();
            Map<String, String> msg = (Map<String, String>)body.get(MqConfig.MSG_BODY) ;
            boolean result = false ;
            if(msg.equals(NOTICE_PUBLISH.getValue())){
                result = noticeMessageProvider.addNoticeMessage(tag, msg);
            }

            if(!result){
                log.error(String.format("NoticesMessageListener process process error: %s", message));
            }
        }catch (Throwable e){
            log.error("NoticesMessageListener process do exception", e);
        }


    }

}
