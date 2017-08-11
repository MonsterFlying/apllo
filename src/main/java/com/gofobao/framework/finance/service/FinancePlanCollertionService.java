package com.gofobao.framework.finance.service;

import com.gofobao.framework.finance.entity.FinancePlanCollection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/8/10.
 */
public interface FinancePlanCollertionService {
    FinancePlanCollection save(FinancePlanCollection FinancePlanCollection);

    List<FinancePlanCollection> save(List<FinancePlanCollection> FinancePlanCollection);

    List<FinancePlanCollection> findList(Specification<FinancePlanCollection> specification);

    List<FinancePlanCollection> findList(Specification<FinancePlanCollection> specification, Sort sort);

    List<FinancePlanCollection> findList(Specification<FinancePlanCollection> specification, Pageable pageable);

    long count(Specification<FinancePlanCollection> specification);
}
