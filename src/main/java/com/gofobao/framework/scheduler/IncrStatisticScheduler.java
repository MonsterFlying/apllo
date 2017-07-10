package com.gofobao.framework.scheduler;

import com.gofobao.framework.helper.DateHelper;

import java.util.Date;

/**
 * Created by Zeke on 2017/7/10.
 */
public class IncrStatisticScheduler {

    public void process(){
        Date startDate = DateHelper.beginOfDate(DateHelper.subDays(new Date(), 1));
        Date endDate = DateHelper.endOfDate(startDate);

    }
}
