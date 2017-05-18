package com.gofobao.framework.message.provider;

/**
 * 短信发送商配置
 * Created by Max on 17/2/22.
 */
public class SmsServerConfig {
    /**
     * 短信配置
     */
    private AbstractSMSConfig config;

    /**
     * 服务商品发送类
     */
    private SmsInterfaceService service;

    public SmsServerConfig() {
    }

    public SmsServerConfig(AbstractSMSConfig config, SmsInterfaceService service) {

        this.config = config;
        this.service = service;
    }

    public AbstractSMSConfig getConfig() {
        return config;
    }

    public void setConfig(AbstractSMSConfig config) {
        this.config = config;
    }

    public SmsInterfaceService getService() {
        return service;
    }

    public void setService(SmsInterfaceService service) {
        this.service = service;
    }
}
