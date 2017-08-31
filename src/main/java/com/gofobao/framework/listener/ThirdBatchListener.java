package com.gofobao.framework.listener;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
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
        log.info(String.format("即信批处理消息队列处理开始: %s", message));
        Preconditions.checkNotNull(message, "BorrowListener process message is empty");
        Map<String, Object> body = gson.fromJson(message, TypeTokenContants.MAP_TOKEN);
        Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "BorrowListener process tag is empty ");
        Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "BorrowListener process body is empty ");
        String tag = body.get(MqConfig.MSG_TAG).toString();
        Map<String, String> msg = (Map<String, String>) body.get(MqConfig.MSG_BODY);
        if (tag.equals(MqTagEnum.BATCH_DEAL.getValue())) {
            boolean state = false ;
            try {
                state = thirdBatchProvider.batchDeal(msg);
            } catch (Throwable e) {
                log.error("批次处理失败!");
            }
            if(state){
                log.info(String.format("即信批处理消息队列处理成功: %s", message));
            }else{
                log.error(String.format("即信批处理消息队列处理失败: %s", message));
            }
        }
    }
}
