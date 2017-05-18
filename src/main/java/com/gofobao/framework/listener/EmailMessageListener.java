package com.gofobao.framework.listener;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.gofobao.framework.listener.providers.CommonSmsProvider;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Max on 17/5/17.
 */
@Slf4j
@Component
public class EmailMessageListener implements MessageListener {
    @Autowired
    CommonSmsProvider commonSmsProvider ;

    Gson gson = new GsonBuilder().create() ;

    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {

        return Action.ReconsumeLater;
    }
}
