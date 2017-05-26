package com.gofobao.framework.core.ons.helper;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import com.gofobao.framework.core.ons.config.OnsMessage;
import com.gofobao.framework.core.ons.config.OnsTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Max on 2017/5/17.
 */
@Component
@Slf4j
public class ApolloMQHelper {
    // @Autowired
    ProducerBean smsProducerBean ;

    // @Autowired
    ProducerBean noticProducerBean ;

    // @Autowired
    ProducerBean emailProducerBean ;

    // @Autowired
    ProducerBean autoTenderProducerBean ;


    public boolean send(OnsMessage onsMessage){
        checkNotNull(onsMessage, "ApolloMQHelper send params: onsMessage is null ") ;
        ProducerBean producerBean = getProducerBean(onsMessage.getTopic());
        if(ObjectUtils.isEmpty(producerBean)) {
            log.error("ApolloMQHelper getProducerBean result is null");
            return false ;
        }

        Message message = new Message(onsMessage.getTopic(), onsMessage.getTag(), onsMessage.getBody().getBytes() ) ;
        // delay Time
        if(onsMessage.getDelayTime() > 0){
            message.setStartDeliverTime(System.currentTimeMillis() + onsMessage.getDelayTime() * 1000);
        }else if(!ObjectUtils.isEmpty(onsMessage.getStartDoWork())){
            message.setStartDeliverTime(onsMessage.getStartDoWork().getTime());
        }

        SendResult rs = producerBean.send(message);
        log.info(String.format("aliyun ons send success：Topic=%s, MessageId=%s", rs.getTopic(), rs.getMessageId()));
        return true ;
    }


    private ProducerBean getProducerBean(String topic) {
        checkNotNull(topic, "ApolloMQHelper send params: topic is null ") ;
        ProducerBean bean ;

        switch (topic){
            case OnsTopics.TOPIC_SMS:
                bean = smsProducerBean ;
                break;
            case OnsTopics.TOPIC_EMAIL:
                bean = emailProducerBean ;
                break;

            case OnsTopics.TOPIC_NOTIC:
                bean = noticProducerBean ;
                break;
            case OnsTopics.TOPIC_AUTO_TENDER:
                bean = autoTenderProducerBean ;
                break;
            default:
                bean = null ;
        }

        return bean ;
    }

}
