package com.gofobao.framework.finance.repository;

import com.gofobao.framework.finance.entity.FinancePlan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

/**
 * Created by Zeke on 2017/8/10.
 */
@Repository
public interface FinancePlanRepository extends JpaRepository<FinancePlan, Long>, JpaSpecificationExecutor<FinancePlan> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    FinancePlan findById(long id);


    @Query("SELECT plan FROM FinancePlan plan " +
            "WHERE " +
            "plan.status NOT  IN ?1 " +
            "AND " +
            "plan.type=?2 " +
            "ORDER BY plan.id DESC , " +
            "plan.status, plan.moneyYes/plan.money ASC ")
    List<FinancePlan> indexList(List<Integer> status, Integer type, Pageable pageable);
}
