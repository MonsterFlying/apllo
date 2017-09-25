package com.gofobao.framework.listener;

import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.listener.providers.ThirdBatchProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Zeke on 2017/7/18.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_THIRD_BATCH)
public class ThirdBatchListener {

    final Gson gson = new GsonBuilder().create();

    @Autowired
    private ThirdBatchProvider thirdBatchProvider;

    @RabbitHandler
    public void process(String message) {

    }
}
