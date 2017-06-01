package com.gofobao.framework.listener;

import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.listener.providers.BorrowProvider;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Map;

/**
 * Created by Zeke on 2017/5/31.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_BORROW)
public class BorrowListener {

    Gson gson = new GsonBuilder().create();

    private final static String FIRST_VERIFY = "firstVerify";
    private final static String AGAIN_VERIFY = "againVerify";

    @Autowired
    private BorrowProvider borrowProvider;

    @RabbitHandler
    public void process(String message) {
        Preconditions.checkNotNull(message, "SmsMessageListener process message is empty");
        Map<String, Object> body = gson.fromJson(message, TypeTokenContants.MAP_TOKEN);

        Long borrowId = NumberHelper.toLong(StringHelper.toString(body.get("borrowId")));
        String type = StringHelper.toString(body.get("type"));

        if (ObjectUtils.isEmpty(borrowId) || ObjectUtils.isEmpty(type)) {
            log.error("borrow listen 参数缺少");
        }

        switch (type) {
            case FIRST_VERIFY:
                if (borrowProvider.doFirstVerify(borrowId)){
                    log.info("===========================borrow listen============================");
                    log.info("初审成功! borrowId："+borrowId);
                    log.info("====================================================================");
                }else {
                    log.info("===========================borrow listen============================");
                    log.info("初审失败! borrowId："+borrowId);
                    log.info("====================================================================");
                }
                break;
            case AGAIN_VERIFY:
                if (borrowProvider.doAgainVerify(borrowId)){
                    log.info("===========================borrow listen============================");
                    log.info("复审成功! borrowId："+borrowId);
                    log.info("====================================================================");
                }else {
                    log.info("===========================borrow listen============================");
                    log.info("复审失败! borrowId："+borrowId);
                    log.info("====================================================================");
                }
                break;
            default:
                log.error("borrow listen 未找到对应的type");
        }
    }
}
