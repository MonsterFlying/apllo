package com.gofobao.framework.helper;

import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 异常短信发送
 */
@Component
@Slf4j
public class ExceptionEmailHelper {

    @Autowired
    MqHelper mqHelper;

    static final String EMAIL = "gofobaodev@163.com";

    /**
     * 发送异常信息
     * @param subject 标题
     * @param e 异常信息
     */
    public void sendException(String subject, Exception e) {
        try {
            MqConfig config = new MqConfig();
            config.setQueue(MqQueueEnum.RABBITMQ_EMAIL);
            config.setTag(MqTagEnum.SEND_BORROW_PROTOCOL_EMAIL);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.EMAIL, EMAIL,
                            MqConfig.IP, "127.0.0.1",
                            "subject", subject,
                            "content", ExceptionUtils.getStackTrace(e));
            config.setMsg(body);
            mqHelper.convertAndSend(config);
        } catch (Exception ex) {
            log.error("发送异常信息", ex);
        }
    }

    /**
     * 发送异常信息
     *
     * @param msg
     */
    public void sendErrorMessage(String subject, String msg) {
        try {
            MqConfig config = new MqConfig();
            config.setQueue(MqQueueEnum.RABBITMQ_EMAIL);
            config.setTag(MqTagEnum.SEND_BORROW_PROTOCOL_EMAIL);
            ImmutableMap<String, String> body = ImmutableMap
                    .of(MqConfig.EMAIL, EMAIL,
                            MqConfig.IP, "127.0.0.1",
                            "subject", subject,
                            "content", msg);
            config.setMsg(body);
            mqHelper.convertAndSend(config);
        } catch (Exception ex) {
            log.error("发送异常信息", ex);
        }
    }
}
