package com.gofobao.framework.listener;

import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Created by Zeke on 2017/8/14.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_FINANCE_PLAN)
public class FinancePlanListener {
}
