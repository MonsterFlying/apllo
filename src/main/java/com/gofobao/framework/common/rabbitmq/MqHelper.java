package com.gofobao.framework.common.rabbitmq;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Max on 17/5/26.
 */
@Component
@Slf4j
public class MqHelper {

    @Autowired
    private AmqpTemplate amqpTemplate ;

    private final static Gson GSON = new Gson() ;

    public boolean convertAndSend(MqConfig config) {
        Preconditions.checkNotNull(config.getQueue(), "ApollomqConfig queue is null");
        Preconditions.checkNotNull(config.getMsg(), "ApollomqConfig msg is null");
        Map<String, Object> body = new HashMap<>(2) ;
        Object msg = config.getMsg();
        body.put(MqConfig.MSG_BODY, msg);
        body.put(MqConfig.MSG_TAG, config.getTag().getValue()) ;
        String json = GSON.toJson(body);

        long delayTime = 0 ;
        if(!ObjectUtils.isEmpty(config.getSendTime()) ){
            long sendTime = config.getSendTime().getTime();
            long nowTime = System.currentTimeMillis();
            delayTime = sendTime - nowTime ;
            delayTime = delayTime <= 0 ? 0 : delayTime ;
        }

        long finalDelayTime = delayTime;
        amqpTemplate.convertAndSend( MqExchangeContants.DELAY_EXCHANGE, config.getQueue().getValue(), json,  new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setHeader("x-delay", finalDelayTime);
                return message;
            }
        });
        return true ;
    }


}
