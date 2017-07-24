package com.gofobao.framework.common.rabbitmq;

/**
 * Created by Max on 17/5/26.
 */
public enum MqTagEnum {
    SMS_WITHDRAW_CASH("SMS_WITHDRAW_CASH"),
    SMS_RESET_PASSWORD("SMS_RESET_PASSWORD"),
    SMS_SWICTH_PHONE("SMS_SWICTH_PHONE"),
    SMS_BUNDLE("SMS_BUNDLE"),
    SMS_MODIFY_BANK("SMS_MODIFY_BANK"),
    SMS_REST_PAY_PASSWORD("SMS_REST_PAY_PASSWORD"),

    SMS_DEFAULT("SMS_DEFAULT"),
    SMS_EMAIL_BIND("SMS_EMAIL_BIND"),
    SEND_BORROW_PROTOCOL_EMAIL("SEND_BORROW_PROTOCOL_EMAIL"),
    SMS_RESET_PAY_PASSWORD("SMS_RESET_PAY_PASSWORD"),
    SMS_BORROW_SUCCESS("SMS_BORROW_SUCCESS"),
    SMS_RECEIVED_REPAY("SMS_RECEIVED_REPAY"),
    SMS_BORROW_REPAYMENT_PUSH("SMS_BORROW_REPAYMENT_PUSH"),
    SMS_REGISTER("SMS_REGISTER"),
    USER_ACTIVE_REGISTER("SMS_REGISTER"),  // 用户注册
    FIRST_VERIFY("FIRST_VERIFY"), //初审
    AGAIN_VERIFY("AGAIN_VERIFY"), //复审
    AUTO_TENDER("AUTO_TENDER"), //自动投标
    NOTICE_PUBLISH("NOTICE_PUBLISH"), // 站内信通知
    NOTICE_PUSH("NOTICE_PUSH"), // 资金变动
    RECHARGE("RECHARGE"), // 充值
    LOGIN("LOGIN"), // 登录
    GIVE_COUPON("GIVE_COUPON"), // 赠送流量券
    NEW_USER_TENDER("NEW_USER_TENDER"), //新用户投标
    OLD_USER_TENDER("OLD_USER_TENDER"), //老用户投标
    INVITE_USER_REAL_NAME("INVITE_USER_REAL_NAME"),  //邀请用户实名
    INVITE_USER_TENDER("INVITE_USER_TENDER"), //邀请用户投资
    END_CREDIT_BY_NOT_TRANSFER("END_CREDIT_BY_NOT_TRANSFER"),//结束债权非转让
    END_CREDIT_BY_TRANSFER("END_CREDIT_BY_TRANSFER"),//结束债权
    END_CREDIT_ALL("END_CREDIT_ALL"),//结束债权
    BATCH_DEAL("batchDeal"),//批次处理
    REPAY_ALL("repayAll"),//提前结清
    REPAY_ADVANCE("repayAdvance"),//提前结清
    ADVANCE("advance"),//担保人垫付
    REPAY("repay");//立即还款


    private String value;

    private MqTagEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

