package com.gofobao.framework.listener;

import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqQueueEnumContants;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
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

import java.util.Map;

/**
 * Created by Zeke on 2017/5/31.
 */
@Slf4j
@Component
@RabbitListener(queues = MqQueueEnumContants.RABBITMQ_BORROW)
public class BorrowListener {

    Gson gson = new GsonBuilder().create();

    @Autowired
    private BorrowProvider borrowProvider;
    @Autowired
    private BorrowBiz borrowBiz;

    @RabbitHandler
    public void process(String message) {
        try {
            log.info(String.format("标的队列待处理信息%s", message));
            Preconditions.checkNotNull(message, "BorrowListener process message is empty");
            Map<String, Object> body = gson.fromJson(message, TypeTokenContants.MAP_TOKEN);
            Preconditions.checkNotNull(body.get(MqConfig.MSG_TAG), "BorrowListener process tag is empty ");
            Preconditions.checkNotNull(body.get(MqConfig.MSG_BODY), "BorrowListener process body is empty ");
            String tag = body.get(MqConfig.MSG_TAG).toString();
            Map<String, String> msg = (Map<String, String>) body.get(MqConfig.MSG_BODY);
            Long borrowId = NumberHelper.toLong(StringHelper.toString(msg.get(MqConfig.MSG_BORROW_ID)));
            boolean bool = false;
            String processMsg;
            if (tag.equals(MqTagEnum.FIRST_VERIFY.getValue())) {  // 标的初审
                bool = borrowBiz.doFirstVerify(borrowId);
                processMsg = "标的初审";
            } else if (tag.equals(MqTagEnum.AGAIN_VERIFY.getValue())) {  // 复审
                bool = borrowProvider.doAgainVerify(msg);
                processMsg = "标的复审";
            } else {
                processMsg = "其他";
            }

            if (bool) {
                log.info(String.format("操作成功: %s: %s", processMsg, message));
            } else {
                log.error(String.format("操作失败: %s: %s", processMsg, message));
            }

        } catch (Throwable throwable) {
            log.error("borrowListener param error：", throwable);
        }
    }
}
