package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.AdvanceLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/6/7.
 */
public interface AdvanceLogService {
    boolean insert(AdvanceLog advanceLog);

    boolean updateById(AdvanceLog advanceLog);

    AdvanceLog findById(Long id);

    AdvanceLog findByIdLock(Long id);

    List<AdvanceLog> findList(Specification<AdvanceLog> specification);

    List<AdvanceLog> findList(Specification<AdvanceLog> specification, Sort sort);

    List<AdvanceLog> findList(Specification<AdvanceLog> specification, Pageable pageable);

    AdvanceLog findByRepaymentId(Long repaymentId);

    AdvanceLog findByRepaymentIdLock(Long repaymentId);

    long count(Specification<AdvanceLog> specification);
}
