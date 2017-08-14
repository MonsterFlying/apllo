package com.gofobao.framework.finance.service.impl;

import com.gofobao.framework.finance.entity.FinancePlanCollection;
import com.gofobao.framework.finance.repository.FinancePlanCollertionRepository;
import com.gofobao.framework.finance.service.FinancePlanCollertionService;
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
public class FinancePlanCollertionServiceImpl implements FinancePlanCollertionService {
    @Autowired
    private FinancePlanCollertionRepository financePlanCollertionRepository;

    public FinancePlanCollection save(FinancePlanCollection financePlanCollection) {
        return financePlanCollertionRepository.save(financePlanCollection);
    }

    public List<FinancePlanCollection> save(List<FinancePlanCollection> financePlanCollectionList) {
        return financePlanCollertionRepository.save(financePlanCollectionList);
    }

    public List<FinancePlanCollection> findList(Specification<FinancePlanCollection> specification) {
        return financePlanCollertionRepository.findAll(specification);
    }

    public List<FinancePlanCollection> findList(Specification<FinancePlanCollection> specification, Sort sort) {
        return financePlanCollertionRepository.findAll(specification, sort);
    }

    public List<FinancePlanCollection> findList(Specification<FinancePlanCollection> specification, Pageable pageable) {
        return financePlanCollertionRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<FinancePlanCollection> specification) {
        return financePlanCollertionRepository.count(specification);
    }
}
