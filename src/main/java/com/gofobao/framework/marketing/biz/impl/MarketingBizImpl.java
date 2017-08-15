package com.gofobao.framework.marketing.biz.impl;

import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.marketing.biz.MarketingBiz;
import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;
import com.gofobao.framework.marketing.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

@Component
public class MarketingBizImpl implements MarketingBiz {

    @Autowired
    MarketingRedpackRecordService marketingRedpackRecordService;

    @Autowired
    MarketingRedpackRuleService marketingRedpackRuleService;

    @Autowired
    MarketingConditionService marketingConditionService;

    @Autowired
    MarketingService marketingService;

    @Autowired
    MarketingDimentsionService marketingDimentsionService;

    @Override
    public void autoCancelRedpack() {
        Date nowDate = new Date();
        Date startDate = DateHelper.beginOfDate(DateHelper.subDays(nowDate, 2));
        int realSize = 0, pageIndex = 0, pageSize = 30;
        do {
            Pageable pageable = new PageRequest(pageIndex, pageSize);
            List<MarketingRedpackRecord> marketingRedpackRecordList = marketingRedpackRecordService.findByCancelTimeBetween(startDate, nowDate, pageable);
            if (CollectionUtils.isEmpty(marketingRedpackRecordList)) {
                break;
            }

            realSize = marketingRedpackRecordList.size();
            for (MarketingRedpackRecord item : marketingRedpackRecordList) {
                item.setState(3);  // 设置为红包已经过期
            }
            marketingRedpackRecordService.save(marketingRedpackRecordList) ;
            pageIndex++;
        } while (realSize == pageSize);
    }
}
