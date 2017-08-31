package com.gofobao.framework.financial.biz;

import com.gofobao.framework.financial.entity.FinancialScheduler;

public interface FinancialSchedulerBiz {

    /**
     * 保存资金调度
     * @param financialScheduler
     */
    void save(FinancialScheduler financialScheduler);

}
