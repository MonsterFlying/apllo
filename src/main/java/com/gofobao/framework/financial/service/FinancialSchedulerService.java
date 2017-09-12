package com.gofobao.framework.financial.service;

import com.gofobao.framework.financial.entity.FinancialScheduler;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * eve服务
 */
public interface FinancialSchedulerService {

    void save(FinancialScheduler financialScheduler);

    FinancialScheduler findById(long id);

    List<FinancialScheduler>list(Specification<FinancialScheduler> specification);

}
