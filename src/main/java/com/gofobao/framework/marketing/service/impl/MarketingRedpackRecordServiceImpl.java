package com.gofobao.framework.marketing.service.impl;

import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;
import com.gofobao.framework.marketing.repository.MarketingRedpackRecordRepository;
import com.gofobao.framework.marketing.service.MarketingRedpackRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketingRedpackRecordServiceImpl implements MarketingRedpackRecordService {
    @Autowired
    MarketingRedpackRecordRepository marketingRedpackRecordRepository;

    @Override
    public MarketingRedpackRecord save(MarketingRedpackRecord marketingRedpackRecord) {
        return marketingRedpackRecordRepository.save(marketingRedpackRecord) ;
    }

    @Override
    public List<MarketingRedpackRecord> findByMarketingIdAndRedpackRuleIdAndUserIdAndSourceId(Long marketingId, Long redpackRuleId, Long userId, Long sourceId) {
        return marketingRedpackRecordRepository.findByMarketingIdAndRedpackRuleIdAndUserIdAndSourceId(marketingId, redpackRuleId, userId, sourceId);
    }
}
