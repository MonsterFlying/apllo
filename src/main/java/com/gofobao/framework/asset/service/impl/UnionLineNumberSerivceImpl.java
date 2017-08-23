package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.UnionLineNumber;
import com.gofobao.framework.asset.repository.UnionLineNumberReopsitory;
import com.gofobao.framework.asset.service.UnionLineNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/**
 * Created by admin on 2017/8/21.
 */
@Component
public class UnionLineNumberSerivceImpl implements UnionLineNumberService {

    @Autowired
    private UnionLineNumberReopsitory unionLineNumberReopsitory;

    @Override
    public Page<UnionLineNumber> findAll(Specification<UnionLineNumber> specification, Pageable pageable) {

        return unionLineNumberReopsitory.findAll(specification, pageable);
    }
}
