package com.gofobao.framework.marketing.service.impl;

import com.gofobao.framework.marketing.entity.Marketing;
import com.gofobao.framework.marketing.repository.MarketingRepository;
import com.gofobao.framework.marketing.service.MarketingService;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MarketingServiceImpl implements MarketingService {
    @Autowired
    MarketingRepository marketingRepository;

    @Override
    public List<Marketing> findAll(Specification<Marketing> marketingSpecification) {
        List<Marketing> all = marketingRepository.findAll(marketingSpecification);
        return Optional.fromNullable(all).or(Lists.newArrayList());
    }

    @Override
    public List<Marketing> findByDelAndOpenStateAndBeginTimeGreaterThanEqualAndEndTimeLessThanEqualAndIdIn(int del, int openState, Date startTime, Date endTime, List<Long> marketingIdList) {
        return Optional
                .fromNullable(marketingRepository.findByDelAndOpenStateAndBeginTimeGreaterThanEqualAndEndTimeLessThanEqualAndIdIn(del, openState, startTime, endTime, marketingIdList))
                .or(Lists.newArrayList());
    }


}
