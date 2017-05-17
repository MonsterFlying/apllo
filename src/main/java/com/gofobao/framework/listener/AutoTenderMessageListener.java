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
public class AutoTenderMessageListener implements MessageListener {
    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
        return null;
    }
}
