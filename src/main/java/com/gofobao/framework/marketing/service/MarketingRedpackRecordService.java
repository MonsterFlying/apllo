package com.gofobao.framework.marketing.service;

import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;

import java.util.List;

public interface MarketingRedpackRecordService {

    MarketingRedpackRecord save(MarketingRedpackRecord marketingRedpackRecord);

    List<MarketingRedpackRecord> findByMarketingIdAndRedpackRuleIdAndUserIdAndSourceId(Long marketingId, Long redpackRuleId, Long userId, Long sourceId);
}
