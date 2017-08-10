package com.gofobao.framework.finance.service.impl;

import com.gofobao.framework.finance.entity.FinancePlan;
import com.gofobao.framework.finance.repository.FinancePlanRepository;
import com.gofobao.framework.finance.service.FinancePlanService;
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
public class FinancePlanServiceImpl implements FinancePlanService {
    @Autowired
    private FinancePlanRepository financePlanRepository;

    public FinancePlan findById(long id) {
        return financePlanRepository.getOne(id);
    }

    public FinancePlan findByIdLock(long id) {
        return financePlanRepository.findById(id);
    }


    public FinancePlan save(FinancePlan financePlan) {
        return financePlanRepository.save(financePlan);
    }

    public List<FinancePlan> save(List<FinancePlan> financePlanList) {
        return financePlanRepository.save(financePlanList);
    }

    public List<FinancePlan> findList(Specification<FinancePlan> specification) {
        return financePlanRepository.findAll(specification);
    }

    public List<FinancePlan> findList(Specification<FinancePlan> specification, Sort sort) {
        return financePlanRepository.findAll(specification, sort);
    }

    public List<FinancePlan> findList(Specification<FinancePlan> specification, Pageable pageable) {
        return financePlanRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<FinancePlan> specification) {
        return financePlanRepository.count(specification);
    }
}
