package com.gofobao.framework.marketing.repository;

import com.gofobao.framework.marketing.entity.MarketingRedpackRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketingRedpackRuleRepository extends JpaRepository<MarketingRedpackRule, Long> {
}
