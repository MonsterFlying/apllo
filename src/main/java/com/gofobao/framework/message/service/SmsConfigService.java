package com.gofobao.framework.message.service;


import com.gofobao.framework.message.provider.SmsServerConfig;

/**
 * Created by Max on 17/2/22.
 */
public interface SmsConfigService {
    /**
     * 获取短信服务商
     *
     * @return
     * @throws Exception
     */
    SmsServerConfig installSMSServer();
}
