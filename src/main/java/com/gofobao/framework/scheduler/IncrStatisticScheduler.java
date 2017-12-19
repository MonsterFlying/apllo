package com.gofobao.framework.scheduler;

import com.gofobao.framework.system.service.IncrStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Date;

/**
 * Created by Zeke on 2017/7/10.
 */
@Component
@Slf4j
public class IncrStatisticScheduler {


    @Autowired
    private IncrStatisticService incrStatisticService;


   /* @Scheduled(cron = "0 30 0 * * ? ")*/
    @Transactional(rollbackOn = Exception.class)
    public void process() {
        Date date=new Date();
        incrStatisticService.dayStatistic(date);
    }
}
