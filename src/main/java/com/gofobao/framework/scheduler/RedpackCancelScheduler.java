package com.gofobao.framework.scheduler;

import com.gofobao.framework.marketing.biz.MarketingBiz;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 凌晨取消红包过期红包
 *
 * Created by max on 2017/7/10.
 */
@Component
@Slf4j
public class RedpackCancelScheduler {

    @Autowired
    MarketingBiz marketingBiz ;

    // @Scheduled(cron = "0 0 4 * * ?")
    public void process() {
        log.info("红包自动过期调度开始");
        marketingBiz.autoCancelRedpack() ;
        log.info("红包自动过期调度结束");
    }
}
