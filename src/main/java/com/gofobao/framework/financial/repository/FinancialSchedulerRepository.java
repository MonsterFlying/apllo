package com.gofobao.framework.financial.repository;

import com.gofobao.framework.financial.entity.FinancialScheduler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialSchedulerRepository extends JpaRepository<FinancialScheduler, Long> ,JpaSpecificationExecutor<FinancialScheduler>{
}
