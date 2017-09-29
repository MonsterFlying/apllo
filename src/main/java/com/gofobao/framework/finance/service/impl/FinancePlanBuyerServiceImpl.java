package com.gofobao.framework.finance.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.GeSpecification;
import com.gofobao.framework.finance.entity.FinancePlanBuyer;
import com.gofobao.framework.finance.repository.FinancePlanBuyerRepository;
import com.gofobao.framework.finance.service.FinancePlanBuyerService;
import com.gofobao.framework.helper.DateHelper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/8/10.
 */
@Service
public class FinancePlanBuyerServiceImpl implements FinancePlanBuyerService {

    @Autowired
    private FinancePlanBuyerRepository financePlanBuyerRepository;

    /**
     * 检查投标是否太频繁
     *
     * @param planId
     * @param userId
     * @return
     */
    public boolean checkFinancePlanBuyNimiety(Long planId, Long userId) {

        Specification<FinancePlanBuyer> specification = Specifications
                .<FinancePlanBuyer>and()
                .eq("userId", userId)
                .eq("planId", planId)
                .eq("status", 1)
                .predicate(new GeSpecification<FinancePlanBuyer>("createdAt", new DataObject(DateHelper.subMinutes(new Date(), 1))))
                .build();
        List<FinancePlanBuyer> tenderList = financePlanBuyerRepository.findAll(specification);
        return !CollectionUtils.isEmpty(tenderList);
    }

    public FinancePlanBuyer findById(long id) {
        return financePlanBuyerRepository.getOne(id);
    }

    public FinancePlanBuyer findByIdLock(long id) {
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
