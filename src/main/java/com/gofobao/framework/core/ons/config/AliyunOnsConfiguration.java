package com.gofobao.framework.core.ons.config;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import com.gofobao.framework.listener.AutoTenderMessageListener;
import com.gofobao.framework.listener.EmailMessageListener;
import com.gofobao.framework.listener.NoticMessageListener;
import com.gofobao.framework.listener.SmsMessageListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Max on 17/5/16.
 */
@Configuration
public class AliyunOnsConfiguration {

    public static final String SMS_PID = "PID_GFB_SMS";
    public static final String SMS_CID = "CID_GFB_SMS";

    public static final String NOTIC_PID = "PID_GFB_NOTIC";
    public static final String NOTIC_CID = "CID_GFB_NOTIC";

    public static final String EMAIL_PID = "PID_GFB_EMAIL";
    public static final String EMAIL_CID = "CID_GFB_EMAIL";

    public static final String AUTO_TENDER_PID = "PID_GFB_AUTO_TENDER";
    public static final String AUTO_TENDER_CID = "CID_GFB_AUTO_TENDER";


    //@Bean
    public SmsMessageListener smsMessageListener(){
        return new SmsMessageListener();
    }


    //@Bean(destroyMethod = "shutdown", initMethod = "start")
    public ProducerBean smsProducerBean(AliyunOnsAccessKey aliyunOnsAccessKey) {
        return buildProducerBean(aliyunOnsAccessKey, SMS_PID);
    }

    //@Bean(destroyMethod = "shutdown", initMethod = "start")
    public ProducerBean noticProducerBean(AliyunOnsAccessKey aliyunOnsAccessKey) {
        return buildProducerBean(aliyunOnsAccessKey, NOTIC_PID);
    }


    //@Bean(destroyMethod = "shutdown", initMethod = "start")
    public ProducerBean emailProducerBean(AliyunOnsAccessKey aliyunOnsAccessKey) {
        return buildProducerBean(aliyunOnsAccessKey, EMAIL_PID);
    }


    //@Bean(destroyMethod = "shutdown", initMethod = "start")
    public ProducerBean autoTenderProducerBean(AliyunOnsAccessKey aliyunOnsAccessKey) {
        return buildProducerBean(aliyunOnsAccessKey, AUTO_TENDER_PID);
    }

    private ProducerBean buildProducerBean(AliyunOnsAccessKey aliyunOnsAccessKey, String PID) {
        ProducerBean bean = new ProducerBean();
        Properties producerProperties = new Properties();
        producerProperties.setProperty(PropertyKeyConst.ProducerId, PID);
        producerProperties.setProperty(PropertyKeyConst.AccessKey, aliyunOnsAccessKey.getAccessKey());
        producerProperties.setProperty(PropertyKeyConst.SecretKey, aliyunOnsAccessKey.getSecretKey());
        producerProperties.setProperty(PropertyKeyConst.ONSAddr, aliyunOnsAccessKey.getOnsAddr());
        bean.setProperties(producerProperties);
        return bean;
    }

    //@Bean(destroyMethod = "shutdown", initMethod = "start")
    public ConsumerBean smsConsumerBean(AliyunOnsAccessKey aliyunOnsAccessKey, SmsMessageListener smsMessageListener){
        return buildConsumerBean(aliyunOnsAccessKey, SMS_CID, OnsTopics.TOPIC_SMS, smsMessageListener) ;
    }


    //@Bean(destroyMethod = "shutdown", initMethod = "start")
    public ConsumerBean emailConsumerBean(AliyunOnsAccessKey aliyunOnsAccessKey, EmailMessageListener emailMessageListener){
        return buildConsumerBean(aliyunOnsAccessKey, EMAIL_CID,  OnsTopics.TOPIC_EMAIL, emailMessageListener) ;
    }

    //@Bean(destroyMethod = "shutdown", initMethod = "start")
    public ConsumerBean noticConsumerBean(AliyunOnsAccessKey aliyunOnsAccessKey, NoticMessageListener noticMessageListener){
        return buildConsumerBean(aliyunOnsAccessKey, NOTIC_CID,  OnsTopics.TOPIC_NOTIC, noticMessageListener) ;
    }

    //@Bean(destroyMethod = "shutdown", initMethod = "start")
    public ConsumerBean autoTenderConsumerBean(AliyunOnsAccessKey aliyunOnsAccessKey, AutoTenderMessageListener autoTenderMessageListener){
        return buildConsumerBean(aliyunOnsAccessKey, AUTO_TENDER_CID,  OnsTopics.TOPIC_AUTO_TENDER, autoTenderMessageListener) ;
    }

    private ConsumerBean buildConsumerBean(AliyunOnsAccessKey aliyunOnsAccessKey, String CID, String topic, MessageListener messageListener) {
        ConsumerBean bean = new ConsumerBean();
        Properties producerProperties = new Properties();
        producerProperties.setProperty(PropertyKeyConst.ConsumerId, CID);
        producerProperties.setProperty(PropertyKeyConst.AccessKey, aliyunOnsAccessKey.getAccessKey());
        producerProperties.setProperty(PropertyKeyConst.SecretKey, aliyunOnsAccessKey.getSecretKey());
        producerProperties.setProperty(PropertyKeyConst.ONSAddr, aliyunOnsAccessKey.getOnsAddr());
        bean.setProperties(producerProperties);

        Map<Subscription, MessageListener> subscriptionTable = new HashMap<>() ;
        Subscription subscription = new Subscription() ;
        subscription.setExpression("*") ;
        subscription.setTopic(topic) ;
        subscriptionTable.put(subscription, messageListener) ;
        bean.setSubscriptionTable(subscriptionTable) ;
        return bean;
    }


}
