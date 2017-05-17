package com.gofobao.framework.message.provider.emay.support;

import com.gofobao.common.sms.AbstractSMSConfig;

/**
 * 亿美短信配置接口
 * Created by Max on 17/2/16.
 */
public class EmaySMSConfig extends AbstractSMSConfig {
    /**
     * 软件序列号,请通过亿美销售人员获取
     */
    private String sn;
    /**
     * 序列号首次激活时自己设定
     */
    private String key;
    /**
     * 密码,请通过亿美销售人员获取
     */
    private String password;
    /**
     * 亿美短信官方接口
     */
    private String baseUrl = "http://sdk4rptws.eucp.b2m.cn:8080/sdkproxy/";

    public EmaySMSConfig(String sn, String key, String password, String baseUrl) {
        this.sn = sn;
        this.key = key;
        this.password = password;
        this.baseUrl = baseUrl;
    }

    public EmaySMSConfig() {
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
