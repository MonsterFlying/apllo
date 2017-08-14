package com.gofobao.framework.finance.repository;

import com.gofobao.framework.finance.entity.FinancePlanCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Created by Zeke on 2017/8/10.
 */
@Repository
public interface FinancePlanCollertionRepository extends JpaRepository<FinancePlanCollection, Long>, JpaSpecificationExecutor<FinancePlanCollection> {
}
