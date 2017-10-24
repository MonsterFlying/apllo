package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.ApplicationVersion;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * Created by master on 2017/10/23.
 */
public interface ApplicationVersionService {


    List<ApplicationVersion> list(Example example,Sort sort);
}
