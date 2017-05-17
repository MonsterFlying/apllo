package com.gofobao.framework.listener;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by Max on 17/5/17.
 */
@Slf4j
@Component
public class EmailMessageListener implements MessageListener {

    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {

        log.info(String.format("Aliyn ons consume log: %s", message.getBody().toString()) );
        message.getBody();
        message.getKey();
        message.getMsgID();
        message.getTag();
        message.getTopic();
        message.getReconsumeTimes();
        message.getStartDeliverTime() ;
        return Action.ReconsumeLater;
    }
}
