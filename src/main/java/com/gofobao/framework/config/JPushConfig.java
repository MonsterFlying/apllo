package com.gofobao.framework.config;

import cn.jiguang.common.ClientConfig;
import cn.jpush.api.JPushClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Administrator on 2017/7/12 0012.
 */
@Configuration
public class JPushConfig {
    @Value("${gofobao.jiguang.AppKey}")
    private String appKey ;

    @Value("${gofobao.jiguang.MasterSecre}")
    private String MasterSecre ;

    @Bean
    public JPushClient jPushClient() {
        return  new JPushClient(MasterSecre, appKey, null, ClientConfig.getInstance());
    }
}
