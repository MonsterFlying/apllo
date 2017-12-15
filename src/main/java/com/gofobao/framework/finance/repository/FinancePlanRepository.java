package com.gofobao.framework.finance.repository;

import com.gofobao.framework.finance.entity.FinancePlan;
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
/*
    @Query("SELECT plan FROM FinancePlan plan " +
            "WHERE " +
            "plan.status NOT  IN ?1 " +
            "AND " +
            "plan.type=?2 " +
            "ORDER BY  " +
            "plan.status,plan.moneyYes/plan.money ASC ,plan.createdAt DESC ")*/

    @Query(value = "SELECT\n" +
            "  *,\n" +
            " IF(plan.`status` = 1 AND plan.`money_yes` = plan.`money`, 3, plan.`status`) AS sort\n" +
            "FROM `gfb_finance_plan` AS plan\n" +
            "WHERE plan.`status` IN ?1\n" +
            "AND  plan.type=?2\n" +
            "ORDER BY sort ASC, plan.id DESC LIMIT ?3,?4", nativeQuery = true)
    List<FinancePlan> indexList(List<Integer> statusArray, Integer type, Integer pageIndex, Integer pageSize);
}
