package com.gofobao.framework.marketing.service.impl;

import com.gofobao.framework.marketing.repository.MarketingConditionRepository;
import com.gofobao.framework.marketing.service.MarketingConditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MarketingConditionServiceImpl implements MarketingConditionService {
    @Autowired
    MarketingConditionRepository marketingConditionRepository;
}
