package com.gofobao.framework.scheduler;

import com.gofobao.framework.member.biz.impl.BrokerBounsBizImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
@Slf4j
public class UserBonusScheduler {

    @Autowired
    private BrokerBounsBizImpl brokerBounsBiz;

    /**
     * 理财师提成
     */
    @Scheduled(cron = "0 35 23 * * ? ")
    public void brokerProcess() {
        Date nowDate = new Date();
        brokerBounsBiz.pushMoney(nowDate);
    }

    /**
     * 天提成
     */
    @Scheduled(cron = "0 30 23 * * ? ")
    public void dayProcess() {
        Date nowDate = new Date();
        brokerBounsBiz.dayPushMoney(nowDate);
    }

    /**
     * 月提成
     */
    @Scheduled(cron = "0 35 23 1 * ? ")
    @Transactional(rollbackFor = Exception.class)
    public void monthProcess() {
        Date nowDate = new Date();
        brokerBounsBiz.monthPushMoney(nowDate);
    }
}
