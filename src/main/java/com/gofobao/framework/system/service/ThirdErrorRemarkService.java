package com.gofobao.framework.system.service;

import com.gofobao.framework.system.entity.ThirdErrorRemark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/8/25.
 */
public interface ThirdErrorRemarkService {
    ThirdErrorRemark findById(long id);

    List<ThirdErrorRemark> findList(Specification<ThirdErrorRemark> thirdErrorRemarkSpecification);

    List<ThirdErrorRemark> findList(Specification<ThirdErrorRemark> thirdErrorRemarkSpecification, Sort sort);

    List<ThirdErrorRemark> findList(Specification<ThirdErrorRemark> thirdErrorRemarkSpecification, Pageable pageable);

    long count(Specification<ThirdErrorRemark> thirdErrorRemarkSpecification);

    ThirdErrorRemark save(ThirdErrorRemark thirdErrorRemark);
}
