package com.gofobao.framework.marketing.service.impl;

import com.gofobao.framework.marketing.repository.MarketingRepository;
import com.gofobao.framework.marketing.service.MarketingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MarketingServiceImpl implements MarketingService {
    @Autowired
    MarketingRepository marketingRepository;
}
