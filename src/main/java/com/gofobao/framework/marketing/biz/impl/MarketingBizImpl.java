package com.gofobao.framework.marketing.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.marketing.biz.MarketingBiz;
import com.gofobao.framework.marketing.constans.MarketingConstans;
import com.gofobao.framework.marketing.entity.Marketing;
import com.gofobao.framework.marketing.entity.MarketingRedpackRecord;
import com.gofobao.framework.marketing.service.*;
import com.gofobao.framework.system.vo.response.Event;
import com.gofobao.framework.system.vo.response.VoEventWarpRes;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
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
            marketingRedpackRecordService.save(marketingRedpackRecordList);
            pageIndex++;
        } while (realSize == pageSize);
    }

    @Override
    public ResponseEntity<VoEventWarpRes> list() {
        VoEventWarpRes warpRes = VoBaseResp.ok("查询成功", VoEventWarpRes.class);
        Specification<Marketing> specification = Specifications.<Marketing>and()
                .eq("openState", MarketingConstans.open)
                .eq("del", MarketingConstans.valid)
                .build();
        List<Marketing> marketings = marketingService.findAll(specification);

        if (CollectionUtils.isEmpty(marketings)) {
            return ResponseEntity.ok(warpRes);
        }

        List<Event> events = Lists.newArrayList();
        marketings.forEach(f -> {
            Event event = new Event();
            event.setTitle(f.getTitel());
            event.setMarketingType(f.getMarketingType());
            event.setIntroduction(f.getIntroduction());
            event.setTargerUrl(f.getTargerUrl());
            event.setBeginAt(DateHelper.dateToString(f.getBeginTime(),DateHelper.DATE_FORMAT_YMD));
            event.setViewUrl(f.getViewUrl());
            events.add(event);
        });
        warpRes.setEvents(events);
        return ResponseEntity.ok(warpRes);
    }
}
