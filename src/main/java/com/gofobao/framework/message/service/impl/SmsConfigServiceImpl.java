package com.gofobao.framework.message.service.impl;


import com.gofobao.framework.message.entity.SmsConfigEntity;
import com.gofobao.framework.message.provider.SmsServerConfig;
import com.gofobao.framework.message.provider.SmsServerConfigContants;
import com.gofobao.framework.message.provider.emay.support.EmaySMSConfig;
import com.gofobao.framework.message.provider.emay.support.SmsInterfaceServiceImpl;
import com.gofobao.framework.message.repository.SmsConfigRepository;
import com.gofobao.framework.message.service.SmsConfigService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


/**
 * 短信服务商配置获取
 * Created by Max on 17/2/22.
 */

@Service
@Slf4j
public class SmsConfigServiceImpl implements SmsConfigService {


    @Autowired
    SmsConfigRepository smsConfigRepository ;

    LoadingCache<Integer, Map<String,String>> smsConfigCaceh = CacheBuilder.newBuilder()
            .maximumSize(8)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build(new CacheLoader<Integer, Map<String,String>>() {
                @Override
                public Map<String,String> load(Integer alias) throws Exception {
                    return findSmsConfigByAlias(1) ;
                }
            }) ;


    /**
     * 查找激活的域名
     * @param active 是否激活
     * @return
     */
    private Map<String,String> findSmsConfigByAlias(Integer active) {
        SmsConfigEntity entity = new SmsConfigEntity() ;
        entity.setIsActive(1);
        entity.setIsDel(0);
        Example<SmsConfigEntity> example = Example.of(entity) ;
        SmsConfigEntity one = smsConfigRepository.findOne(example);
        if(ObjectUtils.isEmpty(one)){
            log.error("SmsConfigServiceImpl findSmsConfigByAlias sms provider is null");
            return null ;
        }

        Map<String, String> result = new HashMap<>() ;
        result.put("config", one.getConfig()) ;
        result.put("alias",  one.getAliasCode()) ;
        return result;
    }


    @Override
    public SmsServerConfig installSMSServer(){

        Map<String, String> smsConfigmap = null;
        try {
            smsConfigmap = smsConfigCaceh.get(1);
        } catch (Throwable e) {
            log.error("SmsConfigServiceImpl installSMSServer sms provider is empty ");
            return null ;
        }

        Gson gson = new GsonBuilder().create() ;
        String alias = smsConfigmap.get("alias");
        SmsServerConfig smsServerConfig = null;

        switch (alias){
            case  SmsServerConfigContants.SMS_SERVER_CONFIG_EMAY:
                smsServerConfig = new SmsServerConfig();
                EmaySMSConfig emaySMSConfig = gson.fromJson(smsConfigmap.get("config"), new TypeToken<EmaySMSConfig>() {
                }.getType());
                smsServerConfig.setConfig(emaySMSConfig);
                smsServerConfig.setService(new SmsInterfaceServiceImpl());
                break;

            default:
                log.error("SmsConfigServiceImpl installSMSServer sms provider is empty ");
        }

        return smsServerConfig;
    }
}
