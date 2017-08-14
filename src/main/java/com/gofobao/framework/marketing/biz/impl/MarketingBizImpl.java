package com.gofobao.framework.marketing.biz.impl;

import com.gofobao.framework.marketing.biz.MarketingBiz;
import com.gofobao.framework.marketing.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MarketingBizImpl implements MarketingBiz {

    @Autowired
    MarketingRedpackRecordService marketingRedpackRecordService;

    @Autowired
    MarketingRedpackRuleService marketingRedpackRuleService ;

    @Autowired
    MarketingConditionService marketingConditionService ;

    @Autowired
    MarketingService marketingService;

    @Autowired
    MarketingDimentsionService marketingDimentsionService;
}
