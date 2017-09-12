package com.gofobao.framework.financial.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.financial.biz.FinancialSchedulerBiz;
import com.gofobao.framework.financial.entity.FinancialScheduler;
import com.gofobao.framework.financial.service.FinancialSchedulerService;
import com.gofobao.framework.helper.DateHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

@Component
public class FinancialSchedulerBizImpl implements FinancialSchedulerBiz {

    @Autowired
    FinancialSchedulerService financialSchedulerService;

    @Override
    public void save(FinancialScheduler financialScheduler) {
        financialSchedulerService.save(financialScheduler);
    }

    @Override
    public boolean isExecute(String type) {
        Date nowDate = new Date();
        Specification<FinancialScheduler> schedulerSpecification = Specifications.<FinancialScheduler>and()
                .eq("state", 1)
                .eq("type", type)
                .between("createAt", new Range<>(DateHelper.beginOfDate(nowDate), DateHelper.endOfDate(nowDate)))
                .build();
        List<FinancialScheduler> financialSchedulerList = financialSchedulerService.list(schedulerSpecification);
        return CollectionUtils.isEmpty(financialSchedulerList) ? false :true;
    }
}
