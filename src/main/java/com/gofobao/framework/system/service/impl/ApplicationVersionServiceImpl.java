package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.Application;
import com.gofobao.framework.system.entity.ApplicationVersion;
import com.gofobao.framework.system.entity.Banner;
import com.gofobao.framework.system.repository.ApplicationRepository;
import com.gofobao.framework.system.repository.ApplicationVersionRepository;
import com.gofobao.framework.system.service.ApplicationVersionService;
import com.gofobao.framework.system.vo.response.IndexBanner;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by master on 2017/10/23.
 */
@Component
public class ApplicationVersionServiceImpl implements ApplicationVersionService {

    @Autowired
    ApplicationVersionRepository applicationVersionRepository;

    @Autowired
    ApplicationRepository applicationRepository ;

    final  LoadingCache<String /* aliasName */,  ApplicationVersion> applicationVersionCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader <String, ApplicationVersion>() {
                @Override
                public ApplicationVersion load(String aliasName) throws Exception {
                    Application application = applicationRepository.findTopByAliasName(aliasName) ;
                    Preconditions.checkNotNull(application,"application record is empty") ;
                    // application id
                    Integer applicationId = application.getId();
                    ApplicationVersion applicationVersion = applicationVersionRepository.findTopByApplicationIdOrderByIdDesc(applicationId) ;
                    Preconditions.checkNotNull(applicationVersion, "applicationVersion record is empty")  ;
                    return applicationVersion;
                }
            });


    @Override
    public ApplicationVersion getNewApplicationVersion(String aliasName) {
        try {
            return applicationVersionCache.get(aliasName) ;
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}
