package com.gofobao.framework.scheduler;

import com.gofobao.framework.helper.DateHelper;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

/**
 * Created by Zeke on 2017/7/10.
 */
public class IncrStatisticScheduler {

    @Scheduled(cron = "0 30 0 * * ? ")
    public void process(){
        Date startDate = DateHelper.beginOfDate(DateHelper.subDays(new Date(), 1));
        Date endDate = DateHelper.endOfDate(startDate);

    }
}
