package com.gofobao.framework.marketing.repository;

import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;
import com.gofobao.framework.marketing.entity.MarketingRedpackRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketingRedpackRecordRepository extends JpaRepository<MarketingRedpackRecord, Long> {

    MarketingRedpackRule findTopByMarketingIdAndDel(Long marketingId, int del);

    List<MarketingRedpackRecord> findByMarketingIdAndRedpackRuleIdAndUserIdAndSourceId(Long marketingId, Long redpackRuleId, Long userId, Long sourceId);
}
