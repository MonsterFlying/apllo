package com.gofobao.framework.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Zeke on 2017/7/24.
 */
@Slf4j
@Component
public class DealThirdBatchScheduler {

    @Scheduled(cron = "0 0 0/1 * * ? ")
    public void process() {
        //1.查询未处理 参数校验成功的批次 gfb_third_batch_log
        //1.
    }
}
