package com.gofobao.framework.listener;

import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.listener.providers.BorrowProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Zeke on 2017/5/31.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_BORROW)
public class BorrowListener {

    @Autowired
    private BorrowProvider borrowProvider;

    @RabbitHandler
    public void process(String message) {

    }
}
