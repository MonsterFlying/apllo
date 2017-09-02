package com.gofobao.framework.scheduler;

import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.asset.biz.CurrentIncomeLogBiz;
import com.gofobao.framework.financial.biz.FinancialSchedulerBiz;
import com.gofobao.framework.financial.entity.FinancialScheduler;
import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

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

    @Autowired
    JixinTxDateHelper jixinTxDateHelper;

    @Autowired
    FinancialSchedulerBiz financialSchedulerBiz;

    private final Gson gson = new Gson();

    @Scheduled(cron = "0 0 3 * * ?")
    public void process() {
        log.info("EVE对账系统任务调度启动！");
        boolean state = false;
        String date = jixinTxDateHelper.getSubDateStr(1);
        String resMsg ;
        try {
            resMsg = "调度成功" ;
            state = fundStatisticsBiz.doEve(date);
        } catch (Throwable e) {
            resMsg = e.getMessage();
        }

        Date nowDate = new Date();
        try {
            FinancialScheduler financialScheduler = new FinancialScheduler();
            ImmutableMap<String, String> data = ImmutableMap.of("date", date);
            financialScheduler.setData(gson.toJson(data));
            financialScheduler.setDoNum(1);
            financialScheduler.setName(String.format("%sEVE调度失败", date));
            financialScheduler.setResMsg(resMsg);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setState(state ? 1 : 0);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setType("EVE");
            financialSchedulerBiz.save(financialScheduler);
        } catch (Exception e) {
            log.error("EVE保存调度信息失败", e);
        }

        try {
            resMsg = "调度成功" ;
            state = fundStatisticsBiz.doAleve(date);
        } catch (Exception e) {
            resMsg = e.getMessage();
        }
        try {
            FinancialScheduler financialScheduler = new FinancialScheduler();
            ImmutableMap<String, String> data = ImmutableMap.of("date", date);
            financialScheduler.setData(gson.toJson(data));
            financialScheduler.setDoNum(1);
            financialScheduler.setName(String.format("%sALEVE调度失败", date));
            financialScheduler.setResMsg(resMsg);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setState(state ? 1 : 0);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setType("ALEVE");
            financialSchedulerBiz.save(financialScheduler);
        } catch (Exception e) {
            log.error("ALEVE保存调度信息失败", e);
        }

        //处理活期收益
        try {
            if (state) {
                state = currentIncomeLogBiz.process(date);
            }
        } catch (Exception e) {
            resMsg = e.getMessage();
        }
        try {
            FinancialScheduler financialScheduler = new FinancialScheduler();
            ImmutableMap<String, String> data = ImmutableMap.of("date", date);
            financialScheduler.setData(gson.toJson(data));
            financialScheduler.setDoNum(1);
            financialScheduler.setName(String.format("%s活期收益调度失败", date));
            financialScheduler.setResMsg(resMsg);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setState(state ? 1 : 0);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setType("CURR_INCOME");
            financialSchedulerBiz.save(financialScheduler);
        } catch (Exception e) {
            log.error("活期调度保存调度信息失败", e);
        }
    }
}
