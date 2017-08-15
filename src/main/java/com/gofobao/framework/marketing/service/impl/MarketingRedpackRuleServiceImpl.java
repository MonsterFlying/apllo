package com.gofobao.framework.marketing.service.impl;

import com.gofobao.framework.marketing.entity.MarketingRedpackRule;
import com.gofobao.framework.marketing.repository.MarketingRedpackRecordRepository;
import com.gofobao.framework.marketing.repository.MarketingRedpackRuleRepository;
import com.gofobao.framework.marketing.service.MarketingRedpackRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MarketingRedpackRuleServiceImpl implements MarketingRedpackRuleService {

    @Autowired
    MarketingRedpackRuleRepository marketingRedpackRuleRepository ;


    @Override
    public MarketingRedpackRule findTopByMarketingIdAndDel(Long marketingId, int del) {
        return marketingRedpackRuleRepository.findTopByMarketingIdAndDel(marketingId, del) ;
    }
}
