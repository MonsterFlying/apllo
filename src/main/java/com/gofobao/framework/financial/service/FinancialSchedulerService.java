package com.gofobao.framework.financial.service;

import com.gofobao.framework.financial.entity.FinancialScheduler;

/**
 * eve服务
 */
public interface FinancialSchedulerService {

    void save(FinancialScheduler financialScheduler);

    FinancialScheduler findById(long id);
}
