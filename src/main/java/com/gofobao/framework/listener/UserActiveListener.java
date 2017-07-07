package com.gofobao.framework.listener;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.listener.providers.UserActiveProvider;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户活动MQ
 * Created by Max on 17/6/1.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_USER_ACTIVE)
public class UserActiveListener {
    private static final Gson GSON = new Gson() ;

    @Autowired
    private UserActiveProvider userActiveProvider ;


    @RabbitHandler
    public void process(String message) {
        log.info(String.format("UserActiveListener process detail: %s", message));
        try {
            Preconditions.checkNotNull(message, "UserActiveListener process message is empty") ;
            Map<String, Object> body = GSON.fromJson(message, TypeTokenContants.MAP_TOKEN);
            Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "UserActiveListener process tag is empty ") ;
            Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "UserActiveListener process body is empty ") ;
            String tag = body.get(MqConfig.MSG_TAG).toString();
            Map<String, String> msg = (Map<String, String>)body.get(MqConfig.MSG_BODY) ;
            boolean result = false;
            if(tag.equals(MqTagEnum.USER_ACTIVE_REGISTER.getValue())){  // 用户注册
                result = userActiveProvider.registerActive(msg) ;
            }else if(tag.equalsIgnoreCase(MqTagEnum.RECHARGE.getValue())){ // 充值
                result = userActiveProvider.recharge(msg) ;
            }else if(tag.equalsIgnoreCase(MqTagEnum.LOGIN.getValue())){ // 登录
                result = userActiveProvider.userLogin(msg) ;
            }

            if(!result){
                log.error(String.format("UserActiveListener process process error: %s", message));
            }
        }catch (Exception e){
            log.error("UserActiveListener process do exception", e);
        } catch (Throwable throwable){
            log.error("UserActiveListener process do exception:", throwable);
        }

    }

}
