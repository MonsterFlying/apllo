package com.gofobao.framework.financial.service.impl;

import com.gofobao.framework.financial.entity.FinancialScheduler;
import com.gofobao.framework.financial.repository.FinancialSchedulerRepository;
import com.gofobao.framework.financial.service.FinancialSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FinancialSchedulerServiceImpl implements FinancialSchedulerService {

    @Autowired
    FinancialSchedulerRepository financialSchedulerRepository ;

    @Override
    public void save(FinancialScheduler financialScheduler) {
        financialSchedulerRepository.save(financialScheduler) ;
    }

    @Override
    public FinancialScheduler findById(long id) {
        return financialSchedulerRepository.findOne(id) ;
    }
}
