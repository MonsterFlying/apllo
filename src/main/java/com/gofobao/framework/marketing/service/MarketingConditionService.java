package com.gofobao.framework.marketing.service;

import com.gofobao.framework.marketing.entity.MarketingCondition;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface MarketingConditionService {

    List<MarketingCondition> findAll(Specification<MarketingCondition> specification);

    List<MarketingCondition> findBymarketingIdInAndDel(List<Long> marketingidList, int del);
}
