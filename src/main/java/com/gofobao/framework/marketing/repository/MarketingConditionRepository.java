package com.gofobao.framework.marketing.repository;

import com.gofobao.framework.marketing.entity.MarketingCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketingConditionRepository extends JpaRepository<MarketingCondition, Long>, JpaSpecificationExecutor<MarketingCondition> {
    List<MarketingCondition> findBymarketingIdInAndDel(List<Long> marketingidList, int del);
}
