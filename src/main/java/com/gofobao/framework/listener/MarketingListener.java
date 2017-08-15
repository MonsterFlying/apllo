package com.gofobao.framework.listener;

import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.JacksonHelper;
import com.gofobao.framework.marketing.biz.MarketingBiz;
import com.gofobao.framework.marketing.biz.MarketingProcessBiz;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
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
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_MARKETING)
public class MarketingListener {

    Gson gson = new Gson();
    @Autowired
    MarketingProcessBiz marketingProcessBiz;

    @RabbitHandler
    public void process(String message) {
        log.info(String.format("RedPackageListener process detail: %s", message));
        Preconditions.checkNotNull(message, "RedPackageListener process message is empty");
        try {
            Map<String, Object> body = JacksonHelper.json2map(message);
            Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG));
            Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY));
            Map<String, Object> msg = (Map<String, Object>) body.get(MqConfig.MSG_BODY);
            String s = gson.toJson(msg);
            marketingProcessBiz.process(s);
        } catch (Throwable throwable) {
            log.error("营销数据处理异常异常:", throwable);
        }
    }


}
