package com.gofobao.framework.message.provider;

import java.util.List;

/**
 * 短信业务发送借口
 * Created by Max on 17/2/16.
 */
public interface SMSInterfaceService {

    /**
     * 短信发送
     *
     * @param config 短信服务商配置类
     * @param phones 手机集合
     * @param mssage 短信内容
     * @return true 发送成功； false 发送失败；
     * @throws Exception
     */
    boolean sendMessage(AbstractSMSConfig config, List<String> phones, String mssage) throws Exception;
}
