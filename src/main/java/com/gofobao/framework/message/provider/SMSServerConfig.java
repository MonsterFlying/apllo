package com.gofobao.framework.message.provider;

/**
 * 短信发送商配置
 * Created by Max on 17/2/22.
 */
public class SMSServerConfig {
    /**
     * 短信配置
     */
    private AbstractSMSConfig config;

    /**
     * 服务商品发送类
     */
    private SMSInterfaceService service;

    public SMSServerConfig() {
    }

    public SMSServerConfig(AbstractSMSConfig config, SMSInterfaceService service) {

        this.config = config;
        this.service = service;
    }

    public AbstractSMSConfig getConfig() {
        return config;
    }

    public void setConfig(AbstractSMSConfig config) {
        this.config = config;
    }

    public SMSInterfaceService getService() {
        return service;
    }

    public void setService(SMSInterfaceService service) {
        this.service = service;
    }
}
