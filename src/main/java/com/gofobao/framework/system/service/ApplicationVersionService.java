package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.ApplicationVersion;

/**
 * Created by master on 2017/10/23.
 */
public interface ApplicationVersionService {

    /**
     * 根据别名获取最新app版本
     * @param aliasName
     * @return
     */
    ApplicationVersion getNewApplicationVersion(String aliasName);
}
