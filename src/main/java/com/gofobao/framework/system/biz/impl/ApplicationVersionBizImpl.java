package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.system.biz.ApplicationVersionBiz;
import com.gofobao.framework.system.entity.ApplicationVersion;
import com.gofobao.framework.system.service.ApplicationVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by master on 2017/10/23.
 */
@Service
public class ApplicationVersionBizImpl implements ApplicationVersionBiz {

    @Autowired
    private ApplicationVersionService applicationVersionService;

    @Override
    public void recheckVersion(ApplicationVersion applicationVersion) {
        //当前
        ApplicationVersion applicationVersion1 = new ApplicationVersion();
        applicationVersion1.setApplicationId(applicationVersion.getApplicationId());
        Example example = Example.of(applicationVersion1);
        List<ApplicationVersion> applicationVersions = applicationVersionService.list(example,
                new Sort(Sort.Direction.DESC,"versionId"));

        if (CollectionUtils.isEmpty(applicationVersions)) {

        }
        ApplicationVersion version = applicationVersions.get(0);
        //判断是否
        if(version.getApplicationId().equals(applicationVersion.getApplicationId())){

        }


        return;

    }
}
