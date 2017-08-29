package com.gofobao.framework.scheduler;

import com.gofobao.framework.asset.biz.CurrentIncomeLogBiz;
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

    @Autowired
    CurrentIncomeLogBiz currentIncomeLogBiz;

    @Scheduled(cron = "0 0 3 * * ?")
    public void process() {
        boolean state = false;
        try {
            log.info("平台账单EVE启动");
            state = fundStatisticsBiz.doEVE();
        } catch (Exception e) {
            log.error("EVE对账异常", e);
        }
        log.info(String.format("平台账单EVE运行结果: %s", state? "成功" : "失败"));

        //处理活期收益
        try{
            if(state){
                state = currentIncomeLogBiz.process();
            }else{
                log.error("每日活期收益没有调度");
            }
        }catch (Exception e){
            log.error("活期收益异常", e);
        }
        log.info(String.format("活期收益运行结果: %s", state? "成功" : "失败"));

    }
}
