package com.gofobao.framework.listener;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.gofobao.framework.core.ons.config.OnsTags;
import com.gofobao.framework.listener.providers.CommonSmsProvider;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;

/**
 * Created by Max on 17/5/17.
 */
@Slf4j
@Component
public class SmsMessageListener implements MessageListener {

    @Autowired
    CommonSmsProvider commonSmsProvider ;

    Gson gson = new GsonBuilder().create() ;

    @Override
    public Action consume(Message message, ConsumeContext consumeContext) {
        log.info(String.format("Aliyn ons consume log: %s", message.getBody().toString()) );
        String tag = message.getTag();
        if(StringUtils.isEmpty(tag)){
            log.error(" EmailMessageListener consume message tag is empty");
            return Action.CommitMessage ;
        }

        HashMap<String, String> body = gson.fromJson(new String(message.getBody()), new TypeToken<HashMap<String, String>>() {
        }.getType());

        if(CollectionUtils.isEmpty(body)){
            log.error(" EmailMessageListener consume message body is empty");
            return Action.CommitMessage ;
        }


        boolean sendResult = false;
        switch (tag){
            case OnsTags.SMS_REGISTER:  // 发送注册短信
            case OnsTags.SMS_RESET_PASSWORD: // 充值短信密码
                sendResult = commonSmsProvider.doSendMessageCode(tag, body) ;
                break;
        }

        if(sendResult){
            return Action.CommitMessage ;
        }else {
            log.error(" EmailMessageListener consume: 发送短信失败");
            return Action.CommitMessage;
        }
    }
}
