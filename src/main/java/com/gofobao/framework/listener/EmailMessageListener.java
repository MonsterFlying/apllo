package com.gofobao.framework.listener;

import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Created by Max on 17/5/17.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_EMAIL)
public class EmailMessageListener {

}
