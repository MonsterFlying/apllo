package com.gofobao.framework.listener;

import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.JacksonHelper;
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
 * Created by admin on 2017/6/29.
 */

@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_RED_PACKAGE)
public class RedPackageListener {

    @Autowired
    private RedPackageListener redPackageListener;

    static Gson GSON = new GsonBuilder().create();


    @RabbitHandler
    public void process(String message) {
        log.info(String.format("RedPackageListener process detail: %s", message));
        Preconditions.checkNotNull(message, "RedPackageListener process message is empty");
        try {
            Map<String, Object> body = JacksonHelper.json2map(message);
            Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG));
            Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY));
            String redPackageType = body.get(MqConfig.MSG_TAG).toString(); //红包类型:新用户,积分兑换,广富币对款....
            Map<String, String> msg = (Map<String, String>) body.get(MqConfig.MSG_BODY);
            do {
                if (redPackageType.equals(MqTagEnum.INTEGRAL_EXCHANGE)) {  //积分兑换

                    break;
                }
                if (redPackageType.equals(MqTagEnum.GOFOBI_EXCHANGE)) {//广富币兑换

                    break;
                }
            } while (false);
        } catch (Exception e) {

        }
    }


}
