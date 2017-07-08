package com.gofobao.framework.message.service.impl;

import com.gofobao.framework.message.entity.SmsTemplateEntity;
import com.gofobao.framework.message.repository.SmsTemplateRepository;
import com.gofobao.framework.message.service.SmsTemplateService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Max on 17/5/18.
 */
@Service
@Slf4j
public class SmsTemplateServiceImpl implements SmsTemplateService {


    @Autowired
    SmsTemplateRepository smsTemplateRepository;


    LoadingCache<String/*alias*/, String /*template*/> smsTemplateCache = CacheBuilder.newBuilder()
            .maximumSize(1024)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String key) throws Exception {
                    return findSmsTemplateByAlias(key);
                }
            });


    /**
     * 通过 alias 查找短信模板
     *
     * @param alias
     * @return
     */
    private String findSmsTemplateByAlias(String alias) {
        List<SmsTemplateEntity> templateEntities = smsTemplateRepository.findByAliasCode(alias);
        if(!CollectionUtils.isEmpty(templateEntities)){
            return templateEntities.get(0).getTemplate() ;
        }

        return null ;
    }


    @Override
    public String findSmsTemplate(String alias) {
        try {
            return smsTemplateCache.get(alias) ;
        } catch (Throwable e) {
            log.error("SmsTemplateServiceImpl findSmsTemplate alias is not exists");
        }

        return null ;
    }
}
