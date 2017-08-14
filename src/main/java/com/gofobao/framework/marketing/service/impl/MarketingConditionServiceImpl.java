package com.gofobao.framework.marketing.service.impl;

import com.gofobao.framework.marketing.entity.MarketingCondition;
import com.gofobao.framework.marketing.repository.MarketingConditionRepository;
import com.gofobao.framework.marketing.service.MarketingConditionService;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketingConditionServiceImpl implements MarketingConditionService {
    @Autowired
    MarketingConditionRepository marketingConditionRepository;

    @Override
    public List<MarketingCondition> findAll(Specification<MarketingCondition> specification) {
        List<MarketingCondition> collection = marketingConditionRepository.findAll(specification);
        Optional<List<MarketingCondition>> listOptional = Optional.fromNullable(collection);
        return listOptional.or(Lists.newArrayList());
    }
}
