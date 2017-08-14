package com.gofobao.framework.marketing.service;

import com.gofobao.framework.marketing.entity.MarketingRedpackRule;

public interface MarketingRedpackRuleService {
    MarketingRedpackRule findTopByMarketingIdAndDel(Long marketingId, int del);
}
