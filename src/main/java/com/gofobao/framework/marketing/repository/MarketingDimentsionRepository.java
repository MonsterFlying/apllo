package com.gofobao.framework.marketing.repository;

import com.gofobao.framework.marketing.entity.MarketingDimentsion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketingDimentsionRepository extends JpaRepository<MarketingDimentsion, Long> {
}
