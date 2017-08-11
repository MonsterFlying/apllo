package com.gofobao.framework.finance.service.impl;

import com.gofobao.framework.finance.entity.FinancePlanTenderLog;
import com.gofobao.framework.finance.repository.FinancePlanTenderLogRepository;
import com.gofobao.framework.finance.service.FinancePlanTenderLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Zeke on 2017/8/10.
 */
@Service
public class FinancePlanTenderLogServiceImpl implements FinancePlanTenderLogService{
    @Autowired
    private FinancePlanTenderLogRepository financePlanTenderLogRepository;

    public FinancePlanTenderLog save(FinancePlanTenderLog financePlanTenderLog) {
        return financePlanTenderLogRepository.save(financePlanTenderLog);
    }

    public List<FinancePlanTenderLog> save(List<FinancePlanTenderLog> financePlanTenderLogs) {
        return financePlanTenderLogRepository.save(financePlanTenderLogs);
    }

    public List<FinancePlanTenderLog> findList(Specification<FinancePlanTenderLog> specification) {
        return financePlanTenderLogRepository.findAll(specification);
    }

    public List<FinancePlanTenderLog> findList(Specification<FinancePlanTenderLog> specification, Sort sort) {
        return financePlanTenderLogRepository.findAll(specification, sort);
    }

    public List<FinancePlanTenderLog> findList(Specification<FinancePlanTenderLog> specification, Pageable pageable) {
        return financePlanTenderLogRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<FinancePlanTenderLog> specification) {
        return financePlanTenderLogRepository.count(specification);
    }
}
