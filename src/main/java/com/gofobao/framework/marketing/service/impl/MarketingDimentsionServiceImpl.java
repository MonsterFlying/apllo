package com.gofobao.framework.marketing.service.impl;

import com.gofobao.framework.marketing.entity.MarketingDimentsion;
import com.gofobao.framework.marketing.repository.MarketingDimentsionRepository;
import com.gofobao.framework.marketing.service.MarketingDimentsionService;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketingDimentsionServiceImpl implements MarketingDimentsionService {
    @Autowired
    MarketingDimentsionRepository marketingDimentsionRepository;

    @Override
    public List<MarketingDimentsion> findBymarketingIdInAndDel(List<Long> marketingidList, int del) {
        return Optional
                .fromNullable(marketingDimentsionRepository.findBymarketingIdInAndDel(marketingidList, del))
                .or(Lists.newArrayList());
    }
}
