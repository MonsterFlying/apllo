package com.gofobao.framework.common.rabbitmq;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * Created by Max on 17/5/26.
 */
@Data
public class MqConfig {

    public static final String MSG_BODY = "body";
    public static final String MSG_TAG = "tag";
    public static final String MSG_USER_ID = "userId";
    public static final String MSG_TIME = "time";
    public static final String MSG_BORROW_ID = "borrowId";
    public static final String MSG_TENDER_ID = "tenderId";
    public static final String MSG_ID = "id";

    public static final String IP = "ip";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    private MqQueueEnum queue;
    private MqTagEnum tag;
    private Map<String, String> msg;
    private Date sendTime;
}




