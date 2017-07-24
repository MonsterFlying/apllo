package com.gofobao.framework.scheduler;

import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Zeke on 2017/7/10.
 */
@Component
@Slf4j
public class FundStatisticsScheduler {

    @Autowired
    FundStatisticsBiz fundStatisticsBiz;


    @Scheduled(cron = "0 0 3 * * ?")
    public void process() {
        log.info("平台账单EVE启动");
        boolean state = false;
        try {
            state = fundStatisticsBiz.doEVE();
        } catch (Exception e) {
            log.error("EVE对账异常", e);
        }
        log.info(String.format("平台账单EVE运行结果: %s", state? "成功" : "失败"));
    }
}
