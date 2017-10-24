package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.ApplicationVersion;
import com.gofobao.framework.system.repository.ApplicationVersionRepository;
import com.gofobao.framework.system.service.ApplicationVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by master on 2017/10/23.
 */
@Component
public class ApplicationVersionServiceImpl implements ApplicationVersionService {

    @Autowired
    private ApplicationVersionRepository applicationVersionRepository;



    @Override
    public List<ApplicationVersion> list(Example example,Sort sort) {
        return null;
    }
}
