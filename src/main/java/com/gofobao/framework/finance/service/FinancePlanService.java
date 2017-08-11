package com.gofobao.framework.finance.service;

import com.gofobao.framework.finance.entity.FinancePlan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/8/10.
 */
public interface FinancePlanService {

    FinancePlan findById(long id);

    FinancePlan findByIdLock(long id);

    FinancePlan save(FinancePlan financePlan);

    List<FinancePlan> save(List<FinancePlan> financePlan);

    List<FinancePlan> findList(Specification<FinancePlan> specification);

    List<FinancePlan> findList(Specification<FinancePlan> specification, Sort sort);

    List<FinancePlan> findList(Specification<FinancePlan> specification, Pageable pageable);

    long count(Specification<FinancePlan> specification);
}
