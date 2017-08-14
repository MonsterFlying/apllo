package com.gofobao.framework.marketing.repository;

import com.gofobao.framework.marketing.entity.MarketingDimentsion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketingDimentsionRepository extends JpaRepository<MarketingDimentsion, Long> {
    List<MarketingDimentsion> findBymarketingIdInAndDel(List<Long> marketingidList, int del);
}
