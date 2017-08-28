package com.gofobao.framework.marketing.enums;

import com.google.gson.annotations.SerializedName;

/**
 * 营销类型
 */
public enum MarketingTypeEnum {
    /**
     * 充值
     */
    @SerializedName("RECHARGE")
    RECHARGE("RECHARGE"),

    /**
     * 开户
     */
    @SerializedName("OPEN_ACCOUNT")
    OPEN_ACCOUNT("OPEN_ACCOUNT"),

    /**
     * 投标
     */
    @SerializedName("TENDER")
    TENDER("TENDER"),

    /**
     * 注册
     */
    @SerializedName("REGISTER")
    REGISTER("REGISTER"),

    /**
     * 登录
     */
    @SerializedName("LOGIN")
    LOGIN("LOGIN");

    private String value ;

    MarketingTypeEnum(String value){
        this.value = value ;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
