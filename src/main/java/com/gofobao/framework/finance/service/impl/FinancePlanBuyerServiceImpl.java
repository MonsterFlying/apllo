package com.gofobao.framework.finance.service.impl;

import com.gofobao.framework.finance.entity.FinancePlanBuyer;
import com.gofobao.framework.finance.repository.FinancePlanBuyerRepository;
import com.gofobao.framework.finance.service.FinancePlanBuyerService;
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
public class FinancePlanBuyerServiceImpl implements FinancePlanBuyerService {

    @Autowired
    private FinancePlanBuyerRepository financePlanBuyerRepository;

    public FinancePlanBuyer findById(long id){
        return financePlanBuyerRepository.getOne(id);
    }

    public FinancePlanBuyer findByIdLock(long id){
        return financePlanBuyerRepository.findById(id);
    }

    public FinancePlanBuyer save(FinancePlanBuyer financePlanBuyer) {
        return financePlanBuyerRepository.save(financePlanBuyer);
    }

    public List<FinancePlanBuyer> save(List<FinancePlanBuyer> financePlanBuyerList) {
        return financePlanBuyerRepository.save(financePlanBuyerList);
    }

    public List<FinancePlanBuyer> findList(Specification<FinancePlanBuyer> specification) {
        return financePlanBuyerRepository.findAll(specification);
    }

    public List<FinancePlanBuyer> findList(Specification<FinancePlanBuyer> specification, Sort sort) {
        return financePlanBuyerRepository.findAll(specification, sort);
    }

    public List<FinancePlanBuyer> findList(Specification<FinancePlanBuyer> specification, Pageable pageable) {
        return financePlanBuyerRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<FinancePlanBuyer> specification) {
        return financePlanBuyerRepository.count(specification);
    }
}
