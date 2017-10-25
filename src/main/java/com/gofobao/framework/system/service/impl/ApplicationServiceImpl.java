package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.system.entity.Application;
import com.gofobao.framework.system.repository.ApplicationRepository;
import com.gofobao.framework.system.service.ApplicationService;
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
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Override
    public List<Application> list(Specification<Application> specification) {

        return applicationRepository.findAll();
    }

    @Override
    public List<Application> list(Example example) {

        return applicationRepository.findAll(example,new Sort(Sort.Direction.DESC,"id"));
    }
}
