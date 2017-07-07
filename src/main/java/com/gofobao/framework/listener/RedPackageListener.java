package com.gofobao.framework.listener;

import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.JacksonHelper;
import com.gofobao.framework.listener.providers.RedPackageProvider;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by admin on 2017/6/29.
 */

@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_RED_PACKAGE)
public class RedPackageListener {

    @Autowired
    private RedPackageProvider redPackageProvider;

    @RabbitHandler
    public void process(String message) {
        log.info(String.format("RedPackageListener process detail: %s", message));
        Preconditions.checkNotNull(message, "RedPackageListener process message is empty");
        try {
            Map<String, Object> body = JacksonHelper.json2map(message);
            Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG));
            Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY));
            String redPackageType = body.get(MqConfig.MSG_TAG).toString(); //红包类型:新用户,积分兑换,广富币对款....
            Map<String, Object> msg = (Map<String, Object>) body.get(MqConfig.MSG_BODY);
            do {
                if (redPackageType.equals(MqTagEnum.INVITE_USER_REAL_NAME)) {  //邀请用户开户
                    redPackageProvider.inviteUserTender(msg);
                    break;
                } else if (redPackageType.equals(MqTagEnum.NEW_USER_TENDER)) {  //新用户投标
                    redPackageProvider.newUserRealName(msg);
                    break;
                } else if (redPackageType.equals(MqTagEnum.INVITE_USER_TENDER)) {//邀请用户投资
                    redPackageProvider.inviteUserTender(msg);
                    break;

                } else if (redPackageType.equals(MqTagEnum.OLD_USER_TENDER)) { //老用户投资
                    redPackageProvider.inviteUserTender(msg);
                    break;
                }
                break;
            } while (false);
        }  catch (Throwable throwable){
            log.error("发送红包异常:", throwable);
        }
    }


}
