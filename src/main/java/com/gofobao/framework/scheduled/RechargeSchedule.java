package com.gofobao.framework.scheduled;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/6/26 0026.
 */
@Component
public class RechargeSchedule {


    @Scheduled(fixedRate = 5000)
    public void reportCurrentTime() {
    }

}
