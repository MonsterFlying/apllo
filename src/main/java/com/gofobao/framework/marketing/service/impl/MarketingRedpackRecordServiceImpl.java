package com.gofobao.framework.marketing.service.impl;

import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;
import com.gofobao.framework.marketing.repository.MarketingRedpackRecordRepository;
import com.gofobao.framework.marketing.service.MarketingRedpackRecordService;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class MarketingRedpackRecordServiceImpl implements MarketingRedpackRecordService {
    @Autowired
    MarketingRedpackRecordRepository marketingRedpackRecordRepository;

    @Override
    public MarketingRedpackRecord save(MarketingRedpackRecord marketingRedpackRecord) {
        return marketingRedpackRecordRepository.save(marketingRedpackRecord);
    }

    @Override
    public List<MarketingRedpackRecord> findByMarketingIdAndRedpackRuleIdAndUserIdAndSourceId(Long marketingId, Long redpackRuleId, Long userId, Long sourceId) {
        return marketingRedpackRecordRepository.findByMarketingIdAndRedpackRuleIdAndUserIdAndSourceId(marketingId, redpackRuleId, userId, sourceId);
    }

    @Override
    public List<MarketingRedpackRecord> findByCancelTimeBetween(Date startDate, Date nowDate, Pageable pageable) {
        return Optional.fromNullable(marketingRedpackRecordRepository.findByCancelTimeBetween(startDate, nowDate, pageable)).or(Lists.newArrayList());
    }

    @Override
    public void save(List<MarketingRedpackRecord> marketingRedpackRecordList) {
        marketingRedpackRecordRepository.save(marketingRedpackRecordList);
    }

    @Override
    public List<MarketingRedpackRecord> findByUserIdAndState(Long userId, Integer status, Pageable pageable) {
        return Optional.fromNullable(marketingRedpackRecordRepository.findByUserIdAndStateAndDel(userId, status, 0,  pageable)).or(Lists.newArrayList());
    }

    @Override
    public MarketingRedpackRecord findTopByIdAndUserIdAndDel(Long redPackageId, Long userId, int del) {
        return marketingRedpackRecordRepository.findTopByIdAndUserIdAndDel(redPackageId, userId, del);
    }

    @Override
    public MarketingRedpackRecord findById(long id) {
        return marketingRedpackRecordRepository.findById(id);
    }

    @Override
    public long count(Specification<MarketingRedpackRecord> specifications) {
        return marketingRedpackRecordRepository.count(specifications) ;
    }
}
