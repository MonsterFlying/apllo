package com.gofobao.framework.common.integral;

/**
 * Created by Zeke on 2017/6/7.
 */
public enum  IntegralChangeEnum {
    /**
     * 投资积分
     */
    TENDER("tender"),
    /**
     * 积分折现
     */
    CONVERT("convert"),
    /**
     * 发帖积分
     */
    POST("post"),
    /**
     * 回帖积分
     */
    REPLY("reply"),
    /**
     * 加精华积分
     */
    DIGEST("digest"),
    /**
     * 取消精华积分
     */
    _DIGEST("_DIGEST"),
    /**
     * 签到积分
     */
    SIGN("sign"),
    /**
     * 签到奖励积分
     */
    SIGN_AWARD("sign_award");

    private String value;

    IntegralChangeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
