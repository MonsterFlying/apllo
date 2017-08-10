package com.gofobao.framework.finance.repository;

import com.gofobao.framework.finance.entity.FinancePlanBuyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;

/**
 * Created by Zeke on 2017/8/10.
 */
@Repository
public interface FinancePlanBuyerRepository extends JpaRepository<FinancePlanBuyer, Long>, JpaSpecificationExecutor<FinancePlanBuyer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    FinancePlanBuyer findById(long id);
}
