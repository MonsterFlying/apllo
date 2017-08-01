package com.gofobao.framework.listener;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.listener.providers.TenderProvider;
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
 * Created by Max on 17/5/17.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_TENDER)
public class TenderMessageListener {

    Gson gson = new GsonBuilder().create();
    @Autowired
    private TenderProvider tenderProvider;

    @RabbitHandler
    public void process(String message) {
        Preconditions.checkNotNull(message, "AutoTenderListener process message is empty");
        Map<String, Object> body = gson.fromJson(message, TypeTokenContants.MAP_TOKEN);
        Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "AutoTenderListener process tag is empty ");
        Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "AutoTenderListener process body is empty ");
        String tag = body.get(MqConfig.MSG_TAG).toString();
        Map<String, String> msg = (Map<String, String>) body.get(MqConfig.MSG_BODY);

        Long borrowId = NumberHelper.toLong(StringHelper.toString(msg.get(MqConfig.MSG_BORROW_ID)));
        Long transferId = NumberHelper.toLong(StringHelper.toString(msg.get(MqConfig.MSG_TRANSFER_ID)));
        if (tag.equals(MqTagEnum.AUTO_TENDER.getValue())) {  // 自动投标
            try {
                tenderProvider.autoTender(msg);
                log.info("===========================AutoTenderListener===========================");
                log.info("自动投标成功! borrowId：" + borrowId);
                log.info("========================================================================");
            } catch (Throwable throwable) {
                log.error("自动投标异常:", throwable);
            }
        } else if (tag.equals(MqTagEnum.AUTO_TENDER.getValue())) {  // 自动投标
            try {
                tenderProvider.autoTransfer(msg);
                log.info("===========================AutoTenderListener===========================");
                log.info("自动投递债权转让成功! transferId：" + transferId);
                log.info("========================================================================");
            } catch (Throwable throwable) {
                log.error("自动投递债权转让异常:", throwable);
            }
        } else {
            log.error("AutoTenderListener 未找到对应的type");
        }
    }
}
