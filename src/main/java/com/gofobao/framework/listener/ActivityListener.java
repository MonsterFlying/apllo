package com.gofobao.framework.listener;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.listener.providers.ActivityProvider;
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
 * Created by Zeke on 2017/6/20.
 */
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_ACTIVITY)
@Slf4j
public class ActivityListener {
    Gson gson = new GsonBuilder().create();
    @Autowired
    private ActivityProvider activityProvider;

    @RabbitHandler
    public void process(String message) {
        Preconditions.checkNotNull(message, "BorrowListener process message is empty");
        Map<String, Object> body = gson.fromJson(message, TypeTokenContants.MAP_TOKEN);
        Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "BorrowListener process tag is empty ");
        Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "BorrowListener process body is empty ");
        String tag = body.get(MqConfig.MSG_TAG).toString();
        Map<String, String> msg = (Map<String, String>) body.get(MqConfig.MSG_BODY);

        if (tag.equals(MqTagEnum.GIVE_COUPON.getValue())) { //赠送流量券
            try {
                activityProvider.TenderGiveTrafficCoupon(msg);
            } catch (Exception e) {
                log.error("赠送流量券异常：", e);
            } catch (Throwable throwable){
                log.error("赠送流量券异常:", throwable);
            }
        } else {
            log.error("ActivityListener error：未匹配到tag" + tag);
        }
    }
}
