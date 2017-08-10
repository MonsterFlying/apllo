package com.gofobao.framework.finance.repository;

import com.gofobao.framework.finance.entity.FinancePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

/**
 * Created by Zeke on 2017/8/10.
 */
@Repository
public interface FinancePlanRepository extends JpaRepository<FinancePlan, Long>, JpaSpecificationExecutor<FinancePlan> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    FinancePlan findById(long id);
}
