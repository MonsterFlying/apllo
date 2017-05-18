package com.gofobao.framework.core.ons.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云ONS (消息队列服务)
 * Created by Max on 17/5/16.
 */

@Component
@Data
@ConfigurationProperties(prefix = "aliyun.keys")
@NoArgsConstructor
@AllArgsConstructor
public class AliyunOnsAccessKey {

    private String accessKey ;

    private String secretKey ;

    private String onsAddr ;
}
