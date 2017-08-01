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
    public static final String MSG_TRANSFER_ID = "transferId";
    public static final String MSG_TENDER_ID = "tenderId";
    public static final String MSG_REPAYMENT_ID = "repaymentId";
    public static final String MSG_ID = "id";

    public static final String IP = "ip";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String TIMESTAMP = "timestamp";
    public static final String CONTENT = "content";
    public static final String SOURCE_ID = "sourceId";
    public static final String BATCH_NO = "batch_no";
    public static final String ACQ_RES = "acq_res";

    private MqQueueEnum queue;
    private MqTagEnum tag;
    private Map<String, String> msg;
    private Date sendTime;
}




