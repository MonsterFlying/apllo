package com.gofobao.framework.finance.service;

import com.gofobao.framework.finance.entity.FinancePlanTenderLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/8/10.
 */
public interface FinancePlanTenderLogService {
    FinancePlanTenderLog save(FinancePlanTenderLog FinancePlanTenderLog);

    List<FinancePlanTenderLog> save(List<FinancePlanTenderLog> FinancePlanTenderLog);

    List<FinancePlanTenderLog> findList(Specification<FinancePlanTenderLog> specification);

    List<FinancePlanTenderLog> findList(Specification<FinancePlanTenderLog> specification, Sort sort);

    List<FinancePlanTenderLog> findList(Specification<FinancePlanTenderLog> specification, Pageable pageable);

    long count(Specification<FinancePlanTenderLog> specification);
}
