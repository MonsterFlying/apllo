package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.system.biz.ApplicationBiz;
import com.gofobao.framework.system.entity.Application;
import com.gofobao.framework.system.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by master on 2017/10/23.
 */
@Service
public class ApplicationBizImpl implements ApplicationBiz {
    @Autowired
    private ApplicationService applicationService;
    @Override
    public List<Application> list(Application application) {
        Example<Application> example = Example.of(application);
        return applicationService.list(example);
    }

}
