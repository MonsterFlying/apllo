package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.system.entity.ThirdErrorRemark;
import com.gofobao.framework.system.repository.ThirdErrorRemarkRepository;
import com.gofobao.framework.system.service.ThirdErrorRemarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeke on 2017/8/25.
 */
@Service
public class ThirdErrorRemarkServiceImpl implements ThirdErrorRemarkService{
    @Autowired
    private ThirdErrorRemarkRepository thirdErrorRemarkRepository;

    public ThirdErrorRemark findById(long id) {
        return thirdErrorRemarkRepository.findOne(id);
    }

    public List<ThirdErrorRemark> findList(Specification<ThirdErrorRemark> thirdErrorRemarkSpecification) {
        return thirdErrorRemarkRepository.findAll(thirdErrorRemarkSpecification);
    }

    public List<ThirdErrorRemark> findList(Specification<ThirdErrorRemark> thirdErrorRemarkSpecification, Sort sort) {
        return thirdErrorRemarkRepository.findAll(thirdErrorRemarkSpecification, sort);
    }

    public List<ThirdErrorRemark> findList(Specification<ThirdErrorRemark> thirdErrorRemarkSpecification, Pageable pageable) {
        return thirdErrorRemarkRepository.findAll(thirdErrorRemarkSpecification, pageable).getContent();
    }

    public long count(Specification<ThirdErrorRemark> thirdErrorRemarkSpecification) {
        return thirdErrorRemarkRepository.count(thirdErrorRemarkSpecification);
    }

    public ThirdErrorRemark save(ThirdErrorRemark thirdErrorRemark) {
        return thirdErrorRemarkRepository.save(thirdErrorRemark);
    }

    public ThirdErrorRemark findByIdLock(long id){
        return thirdErrorRemarkRepository.findById(id);
    }
}
