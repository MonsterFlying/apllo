package com.gofobao.framework.financial.biz.impl;

import com.gofobao.framework.financial.biz.FinancialSchedulerBiz;
import com.gofobao.framework.financial.service.FinancialSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FinancialSchedulerBizImpl implements FinancialSchedulerBiz {

    @Autowired
    FinancialSchedulerService financialSchedulerService ;

}
