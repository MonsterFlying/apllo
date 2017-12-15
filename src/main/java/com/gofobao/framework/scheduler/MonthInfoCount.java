package com.gofobao.framework.scheduler;

import com.gofobao.framework.scheduler.service.CountAssetInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

/**
 * Created by xin on 2017/12/13.
 */
public class MonthInfoCount {
    @Autowired
    private CountAssetInfo countAssetInfo;

    /**
     * 每个月初定时统计
     */
    @Scheduled(cron = "0 30 0 * * ?")
    public void process() {
        countAssetInfo.dayStatistic(new Date());
    }
}
