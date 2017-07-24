package com.gofobao.framework.config;

import com.gofobao.framework.common.rabbitmq.MqExchangeContants;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Max on 17/5/26.
 */
@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue smsRabbitmq() {
        return new Queue(MqQueueEnum.RABBITMQ_SMS.getValue(), true);
    }

    @Bean
    public Queue emailRabbitmq() {
        return new Queue(MqQueueEnum.RABBITMQ_EMAIL.getValue(), true);
    }

    @Bean
    public Queue tenderRabbitmq() {
        return new Queue(MqQueueEnum.RABBITMQ_TENDER.getValue(), true);
    }

    @Bean
    public Queue borrowRabbitmq() {
        return new Queue(MqQueueEnum.RABBITMQ_BORROW.getValue(), true);
    }

    @Bean
    public Queue activityRabbitmq() {
        return new Queue(MqQueueEnum.RABBITMQ_ACTIVITY.getValue(), true);
    }

    @Bean
    public Queue noticeRabbitmq() {
        return new Queue(MqQueueEnum.RABBITMQ_NOTICE.getValue(), true);
    }

    @Bean
    public Queue userActiveRabbitmq() {
        return new Queue(MqQueueEnum.RABBITMQ_USER_ACTIVE.getValue(), true);
    }

    @Bean
    public Queue redPackageRabbitmq() {
        return new Queue(MqQueueEnum.RABBITMQ_RED_PACKAGE.getValue(), true);
    }

    @Bean
    public Queue creditRabbitmq() {
        return new Queue(MqQueueEnum.RABBITMQ_CREDIT.getValue(), true);
    }

    @Bean
    public Queue thirdBatchRabbitmq() {
        return new Queue(MqQueueEnum.RABBITMQ_THIRD_BATCH.getValue(), true);
    }

    @Bean
    public Queue repaymentRabbitmq() {
        return new Queue(MqQueueEnum.RABBITMQ_REPAYMENT.getValue(), true);
    }

    @Bean
    DirectExchange delayExchange() {
        DirectExchange directExchange = new DirectExchange(MqExchangeContants.DELAY_EXCHANGE, true, false);
        directExchange.setDelayed(true);
        return directExchange;
    }

    @Bean
    Binding smsRabbitmqBinding(Queue smsRabbitmq, Exchange delayExchange) {
        return BindingBuilder.bind(smsRabbitmq).to(delayExchange).with(MqQueueEnum.RABBITMQ_SMS.getValue()).noargs();
    }


    @Bean
    Binding emailRabbitmqBinding(Queue emailRabbitmq, Exchange delayExchange) {
        return BindingBuilder.bind(emailRabbitmq).to(delayExchange).with(MqQueueEnum.RABBITMQ_EMAIL.getValue()).noargs();
    }


    @Bean
    Binding tenderRabbitmqBinding(Queue tenderRabbitmq, Exchange delayExchange) {
        return BindingBuilder.bind(tenderRabbitmq).to(delayExchange).with(MqQueueEnum.RABBITMQ_TENDER.getValue()).noargs();
    }

    @Bean
    Binding thirdBatchRabbitmqBinding(Queue thirdBatchRabbitmq, Exchange delayExchange) {
        return BindingBuilder.bind(thirdBatchRabbitmq).to(delayExchange).with(MqQueueEnum.RABBITMQ_THIRD_BATCH.getValue()).noargs();
    }

    @Bean
    Binding borrowRabbitmqBinding(Queue borrowRabbitmq, Exchange delayExchange) {
        return BindingBuilder.bind(borrowRabbitmq).to(delayExchange).with(MqQueueEnum.RABBITMQ_BORROW.getValue()).noargs();
    }

    @Bean
    Binding noticeRabbitmqBinding(Queue noticeRabbitmq, Exchange delayExchange) {
        return BindingBuilder.bind(noticeRabbitmq).to(delayExchange).with(MqQueueEnum.RABBITMQ_NOTICE.getValue()).noargs();
    }


    @Bean
    Binding userActiveRabbitmqBinding(Queue userActiveRabbitmq, Exchange delayExchange) {
        return BindingBuilder.bind(userActiveRabbitmq).to(delayExchange).with(MqQueueEnum.RABBITMQ_USER_ACTIVE.getValue()).noargs();
    }

    @Bean
    Binding redPackageRabbitmqBinding(Queue redPackageRabbitmq, Exchange delayExchange) {
        return BindingBuilder.bind(redPackageRabbitmq).to(delayExchange).with(MqQueueEnum.RABBITMQ_RED_PACKAGE.getValue()).noargs();
    }

    @Bean
    Binding creditRabbitmqBinding(Queue creditRabbitmq, Exchange delayExchange) {
        return BindingBuilder.bind(creditRabbitmq).to(delayExchange).with(MqQueueEnum.RABBITMQ_CREDIT.getValue()).noargs();
    }

    @Bean
    Binding repaymentRabbitmqBinding(Queue repaymentRabbitmq, Exchange delayExchange) {
        return BindingBuilder.bind(repaymentRabbitmq).to(delayExchange).with(MqQueueEnum.RABBITMQ_REPAYMENT.getValue()).noargs();
    }

}
