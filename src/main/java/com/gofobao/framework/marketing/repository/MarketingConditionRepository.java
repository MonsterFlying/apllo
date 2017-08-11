package com.gofobao.framework.marketing.repository;

import com.gofobao.framework.marketing.entity.MarketingCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketingConditionRepository extends JpaRepository<MarketingCondition, Long> {
}
