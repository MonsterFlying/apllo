package com.gofobao.framework.api.helper;

/**
 * 即信交易类型
 * Created by Max on 17/5/19.
 */

public enum JixinTxCodeEnum {
    /**
     * 开户类型
     */
    OPEN_ACCOUNT(
            "accountOpen",
            "/escrow/p2p/page/mobile"),

    /**
     * 发送短信验证码
     */
    SMS_CODE_APPLY(
            "smsCodeApply",
            "/escrow/p2p/online");

    private String value;
    private String url;

    private JixinTxCodeEnum(String value, String url) {
        this.value = value;
        this.url = url;
    }

    public String getValue() {
        return value;
    }

    public String getUrl() {
        return url;
    }
}
