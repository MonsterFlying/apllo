package com.gofobao.framework.core.mq.config;

import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Created by Max on 17/5/16.
 */
@Configuration
public class AliyunOnsConfiguration {

    /** 短信 */
    public static final String TOPIC_SMS = "GFB_SMS";
    /** 邮箱 */
    public static final String TOPIC_EMAIL = "GFB_EMAIL";
    /** 通知 */
    public static final String TOPIC_NOTIC = "GFB_NOTIC";
    /** 自动投标 */
    public static final String TOPIC_AUTO_TENDER = "GFB_AUTO_TENDER";

    public static final String SMS_PID = "PID_GFB_SMS";
    public static final String SMS_CID = "CID_GFB_SMS";

    public static final String NOTIC_PID = "PID_GFB_NOTIC";
    public static final String NOTIC_CID = "CID_GFB_NOTIC";

    public static final String EMAIL_PID = "PID_GFB_EMAIL";
    public static final String EMAIL_CID = "CID_GFB_EMAIL";

    public static final String AUTO_TENDER_PID = "PID_GFB_AUTO_TENDER";
    public static final String AUTO_TENDER_CID = "CID_GFB_AUTO_TENDER";


    @Bean
    public ProducerBean smsProducerBean(AliyunOnsAccessKey aliyunOnsAccessKey) {
        return buildProducerBean(aliyunOnsAccessKey, SMS_PID);
    }

    @Bean
    public ProducerBean noticProducerBean(AliyunOnsAccessKey aliyunOnsAccessKey) {
        return buildProducerBean(aliyunOnsAccessKey, NOTIC_PID);
    }


    @Bean
    public ProducerBean emailProducerBean(AliyunOnsAccessKey aliyunOnsAccessKey) {
        return buildProducerBean(aliyunOnsAccessKey, EMAIL_PID);
    }


    @Bean
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

    @Bean
    public ConsumerBean smsConsumerBean(AliyunOnsAccessKey aliyunOnsAccessKey){
        return buildConsumerBean(aliyunOnsAccessKey, SMS_CID) ;
    }


    @Bean
    public ConsumerBean emailConsumerBean(AliyunOnsAccessKey aliyunOnsAccessKey){
        return buildConsumerBean(aliyunOnsAccessKey, EMAIL_CID) ;
    }

    @Bean
    public ConsumerBean noticConsumerBean(AliyunOnsAccessKey aliyunOnsAccessKey){
        return buildConsumerBean(aliyunOnsAccessKey, NOTIC_CID) ;
    }

    @Bean
    public ConsumerBean autoTenderConsumerBean(AliyunOnsAccessKey aliyunOnsAccessKey){
        return buildConsumerBean(aliyunOnsAccessKey, AUTO_TENDER_CID) ;
    }

    private ConsumerBean buildConsumerBean(AliyunOnsAccessKey aliyunOnsAccessKey, String CID) {
        ConsumerBean bean = new ConsumerBean();
        Properties producerProperties = new Properties();
        producerProperties.setProperty(PropertyKeyConst.ConsumerId, CID);
        producerProperties.setProperty(PropertyKeyConst.AccessKey, aliyunOnsAccessKey.getAccessKey());
        producerProperties.setProperty(PropertyKeyConst.SecretKey, aliyunOnsAccessKey.getSecretKey());
        producerProperties.setProperty(PropertyKeyConst.ONSAddr, aliyunOnsAccessKey.getOnsAddr());
        bean.setProperties(producerProperties);
        return bean;
    }


}
