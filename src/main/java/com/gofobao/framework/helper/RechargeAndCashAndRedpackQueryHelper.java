package com.gofobao.framework.helper;

import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 1.即信充值流水确认
 * 2.即信提现流水确认
 * 3.即信红包派发确定
 * 4.即信红包撤回确定
 * 5.即信红包隔日撤回
 */
@Component
@Slf4j
public class RechargeAndCashAndRedpackQueryHelper {


    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    public enum QueryType {
        /**
         * 充值查询
         */
        QUERY_RECHARGE("recharge"),

        /**
         * 提现查询
         */
        QUERY_CASH("cash");


        QueryType(String value) {
            this.value = value;
        }

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Autowired
    MqHelper mqHelper;


    /**
     * 保存通知信息
     *
     * @param type     1.充值流水确认
     * @param userId
     * @param sourceId
     * @param isWeb
     */
    public void save(QueryType type, long userId, long sourceId, boolean isWeb) {
        // 触发时间
        int inTime = isWeb ? 21 : 11; // 查询间隔时间
        Date nowDate = new Date();
        Gson gson = new Gson();
        log.info("触发查询接口");
        MqConfig mqConfig = new MqConfig();
        mqConfig.setQueue(MqQueueEnum.RABBITMQ_USER_ACTIVE); // 用户行为
        mqConfig.setTag(MqTagEnum.OP_JIXIN_QUERY);  // 即信查询
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.MSG_USER_ID, userId + "",
                        MqConfig.SOURCE_ID, sourceId + "",
                        MqConfig.TYPE, type.getValue());

        mqConfig.setSendTime(DateHelper.addMonths(nowDate, inTime));
        mqConfig.setMsg(body);
        try {
            mqHelper.convertAndSend(mqConfig);
        } catch (Throwable e) {
            exceptionEmailHelper.sendErrorMessage("发送即信查询MQ失败", gson.toJson(body));
            log.error("RechargeAndCashAndRedpackQueryHelper save send mq exception", e);
        }
    }
}
