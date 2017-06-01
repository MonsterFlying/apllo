package com.gofobao.framework.config;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Max on 17/5/26.
 */
@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue smsRabbitmq(){
        return new Queue(MqQueueEnum.RABBITMQ_SMS.getValue()) ;
    }


    @Bean
    public Queue emailRabbitmq(){
        return new Queue(MqQueueEnum.RABBITMQ_EMAIL.getValue()) ;
    }


    @Bean
    public Queue autoTenderRabbitmq(){
        return new Queue(MqQueueEnum.RABBITMQ_AUTO_TENDER.getValue()) ;
    }

    @Bean
    public Queue borrowRabbitmq(){
        return new Queue(MqQueueEnum.RABBITMQ_BORROW.getValue()) ;
    }

    @Bean
    public Queue noticeRabbitmq(){
        return new Queue(MqQueueEnum.RABBITMQ_NOTICE.getValue()) ;
    }

}
