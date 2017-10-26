package com.gofobao.framework.system.service;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.system.entity.Application;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by master on 2017/10/23.
 */
public interface ApplicationService {

    List<Application> list(Specification<Application> specification);



    List<Application> list(Example example);
}
