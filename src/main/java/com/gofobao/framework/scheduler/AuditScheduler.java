package com.gofobao.framework.scheduler;

import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.asset.biz.OfflineRechargeSynBiz;
import com.gofobao.framework.financial.biz.NewAleveBiz;
import com.gofobao.framework.financial.biz.NewEveBiz;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * 主要功能
 * 每天五点,
 * 1.发送昨天所有交易流水
 * 2.发送昨天即信交易流水
 * 3.发送两边账户余额
 */
@Component
@Slf4j
@Deprecated
public class AuditScheduler {
    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Autowired
    NewAleveBiz newAleveBiz;

    @Autowired
    NewEveBiz newEveBiz;

    @Autowired
    ExceptionEmailHelper exceptionEmailHelper;

    @Autowired
    OfflineRechargeSynBiz offlineRechargeSynBiz;

    /**
     * 自动投标
     */
   @Scheduled(cron = "0 0 5 * * ?")
    public void process() {
        String date = jixinTxDateHelper.getSubDateStr(1);
        log.info("=======================================");
        log.info(String.format("审计系统启动, 时间: %s", date));
        log.info("=======================================");

        // 进行系统审计
        // newEveBiz.audit(date) ;
    }

}
