package com.gofobao.framework.marketing.service;

import com.gofobao.framework.marketing.entity.MarketingDimentsion;

import java.util.List;

public interface MarketingDimentsionService {

    List<MarketingDimentsion> findBymarketingIdInAndDel(List<Long> marketingidList, int del);
}
