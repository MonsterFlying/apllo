package com.gofobao.framework.marketing.service.impl;

import com.gofobao.framework.marketing.repository.MarketingDimentsionRepository;
import com.gofobao.framework.marketing.service.MarketingDimentsionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MarketingDimentsionServiceImpl implements MarketingDimentsionService {
    @Autowired
    MarketingDimentsionRepository marketingDimentsionRepository ;
}
