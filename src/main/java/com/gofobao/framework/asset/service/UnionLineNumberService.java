package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.UnionLineNumber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Created by admin on 2017/8/21.
 */
public interface UnionLineNumberService {

     Page<UnionLineNumber> findAll(Specification<UnionLineNumber> specification, Pageable pageable);
}
