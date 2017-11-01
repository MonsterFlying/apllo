package com.gofobao.framework.listener;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.listener.providers.CreditProvider;
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
 * Created by Zeke on 2017/7/10.
 */

@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_CREDIT)
@Slf4j
public class CreditListener {

    Gson gson = new GsonBuilder().create();
    @Autowired
    private CreditProvider creditProvider;

    @RabbitHandler
    public void process(String message) {
        Preconditions.checkNotNull(message, "BorrowListener process message is empty");
        Map<String, Object> body = gson.fromJson(message, TypeTokenContants.MAP_TOKEN);
        Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "BorrowListener process tag is empty ");
        Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "BorrowListener process body is empty ");
        String tag = body.get(MqConfig.MSG_TAG).toString();
        Map<String, String> msg = (Map<String, String>) body.get(MqConfig.MSG_BODY);
        Long borrowId = NumberHelper.toLong(StringHelper.toString(msg.get(MqConfig.MSG_BORROW_ID)));

        boolean bool = false;

        if (true) {
            log.error("=============================================================================");
            log.error("说明：结束债权改版，自动结束债权改为手动结束债权，请针对单个用户进行结束债权操作！");
            log.error("=============================================================================");
        } else {
            try {
                bool = creditProvider.endThirdCredit(msg, tag);
            } catch (Throwable throwable) {
                log.error("结束存管债权异常:", throwable);
            }

            if (bool) {
                log.info("===========================BorrowListener===========================");
                log.info("结束存管债权成功! borrowId：" + borrowId);
                log.info("====================================================================");
            } else {
                log.info("===========================BorrowListener===========================");
                log.info("结束存管债权失败! borrowId：" + borrowId);
                log.info("====================================================================");
            }
        }
    }
}
