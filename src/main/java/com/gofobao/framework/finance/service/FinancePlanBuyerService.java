package com.gofobao.framework.finance.service;

import com.gofobao.framework.finance.entity.FinancePlanBuyer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Created by Zeke on 2017/8/10.
 */
public interface FinancePlanBuyerService {

    /**
     * 检查投标是否太频繁
     *
     * @param borrowId
     * @param userId
     * @return
     */
    boolean checkFinancePlanBuyNimiety(Long borrowId, Long userId);

    FinancePlanBuyer findById(long id);

    FinancePlanBuyer findByIdLock(long id);

    FinancePlanBuyer save(FinancePlanBuyer financePlanBuyer);

    List<FinancePlanBuyer> save(List<FinancePlanBuyer> financePlanBuyer);

    List<FinancePlanBuyer> findList(Specification<FinancePlanBuyer> specification);

    List<FinancePlanBuyer> findList(Specification<FinancePlanBuyer> specification, Sort sort);

    List<FinancePlanBuyer> findList(Specification<FinancePlanBuyer> specification, Pageable pageable);

    long count(Specification<FinancePlanBuyer> specification);
}
