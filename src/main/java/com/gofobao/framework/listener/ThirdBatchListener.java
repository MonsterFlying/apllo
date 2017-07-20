package com.gofobao.framework.listener;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.listener.providers.ThirdBatchProvider;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Zeke on 2017/7/18.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_THIRD_BATCH)
public class ThirdBatchListener {

    final Gson gson = new GsonBuilder().create();

    @Autowired
    private ThirdBatchProvider thirdBatchProvider;

    @RabbitHandler
    public void process(String message) {
        Map<String, Object> body = null;
        String tag = null;
        Map<String, String> msg = null;

        Preconditions.checkNotNull(message, "BorrowListener process message is empty");
        body = gson.fromJson(message, TypeTokenContants.MAP_TOKEN);
        Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "BorrowListener process tag is empty ");
        Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "BorrowListener process body is empty ");
        tag = body.get(MqConfig.MSG_TAG).toString();
        msg = (Map<String, String>) body.get(MqConfig.MSG_BODY);
        if (tag.equals(MqTagEnum.BATCH_DEAL.getValue())) {
            try {
                thirdBatchProvider.batchDeal(msg);
            } catch (Throwable e) {
                log.error("ThirdBatchListener error ：", e);
            }
        }
    }
}
