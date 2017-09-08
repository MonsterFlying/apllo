package com.gofobao.framework.scheduler;

import com.gofobao.framework.api.helper.JixinTxDateHelper;
import com.gofobao.framework.asset.biz.AssetSynBiz;
import com.gofobao.framework.asset.biz.CurrentIncomeLogBiz;
import com.gofobao.framework.common.constans.TypeTokenContants;
import com.gofobao.framework.financial.biz.FinancialSchedulerBiz;
import com.gofobao.framework.financial.entity.FinancialScheduler;
import com.gofobao.framework.financial.service.FinancialSchedulerService;
import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

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

    @Autowired
    AssetSynBiz assetSynBiz;

    @Autowired
    FinancialSchedulerService financialSchedulerService ;

    private final Gson gson = new Gson();

    @Scheduled(cron = "0 0 3 * * ?")
    public void process() {
        log.info("EVE对账系统任务调度启动！");
        boolean eveState = false;
        String date = jixinTxDateHelper.getSubDateStr(1);
        String resMsg;
        try {
            resMsg = "调度成功";
            eveState = fundStatisticsBiz.doEve(date);
            if (!eveState) {
                resMsg = "EVE执行下载失败";
            }
        } catch (Throwable e) {
            resMsg = e.getMessage();
        }

        Date nowDate = new Date();
        try {
            FinancialScheduler financialScheduler = new FinancialScheduler();
            ImmutableMap<String, String> data = ImmutableMap.of("date", date);
            financialScheduler.setData(gson.toJson(data));
            financialScheduler.setDoNum(1);
            financialScheduler.setName(String.format("%sEVE调度", date));
            financialScheduler.setResMsg(resMsg);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setState(eveState ? 1 : 0);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setType("EVE");
            financialSchedulerBiz.save(financialScheduler);
        } catch (Exception e) {
            log.error("EVE保存调度信息失败", e);
        }

        //处理活期收益
        try {
            if (eveState) {
                eveState = currentIncomeLogBiz.process(date);
                if (!eveState) {
                    resMsg = "活期收益执行失败";
                } else {
                    resMsg = "活期收益执行成功";
                }
            } else {
                resMsg = "EVE文件未下载成功";
            }
        } catch (Exception e) {
            resMsg = e.getMessage();
        }
        try {
            FinancialScheduler financialScheduler = new FinancialScheduler();
            ImmutableMap<String, String> data = ImmutableMap.of("date", date);
            financialScheduler.setData(gson.toJson(data));
            financialScheduler.setDoNum(1);
            financialScheduler.setName(String.format("%s活期收益调度", date));
            financialScheduler.setResMsg(resMsg);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setState(eveState ? 1 : 0);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setType("CURR_INCOME");
            financialSchedulerBiz.save(financialScheduler);
        } catch (Exception e) {
            log.error("活期调度保存调度信息失败", e);
        }


        boolean aleveState = true;
        try {
            resMsg = "调度成功";
            aleveState = fundStatisticsBiz.doAleve(date);
            if (!aleveState) {
                resMsg = "Aleve执行失败";
            }
        } catch (Exception e) {
            resMsg = e.getMessage();
        }
        try {
            FinancialScheduler financialScheduler = new FinancialScheduler();
            ImmutableMap<String, String> data = ImmutableMap.of("date", date);
            financialScheduler.setData(gson.toJson(data));
            financialScheduler.setDoNum(1);
            financialScheduler.setName(String.format("%sALEVE调度", date));
            financialScheduler.setResMsg(resMsg);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setState(aleveState ? 1 : 0);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setType("ALEVE");
            financialSchedulerBiz.save(financialScheduler);
        } catch (Exception e) {
            log.error("ALEVE保存调度信息失败", e);
        }

        /*// 处理未同步的线下转账
        try {
            resMsg = "调度成功";
            if (aleveState) {
                aleveState = assetSynBiz.doOffLineRechargeByAleve(date);
                if (!aleveState) {
                    resMsg = "全局线下同步充值失败";
                }
            } else {
                resMsg = "ALEVE保存调度信息失败, 导致不能同步线下充值";
            }
        } catch (Exception e) {
            resMsg = e.getMessage();
        }*/
        try {
            FinancialScheduler financialScheduler = new FinancialScheduler();
            ImmutableMap<String, String> data = ImmutableMap.of("date", date);
            financialScheduler.setData(gson.toJson(data));
            financialScheduler.setDoNum(1);
            financialScheduler.setName(String.format("%s全局线下资金调度", date));
            financialScheduler.setResMsg(resMsg);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setState(eveState ? 1 : 0);
            financialScheduler.setUpdateAt(nowDate);
            financialScheduler.setType("OFFLIME_RECHARGE");
            financialSchedulerBiz.save(financialScheduler);
        } catch (Exception e) {
            log.error("活期调度保存调度信息失败", e);
        }


    }

    public void scheduler(long id) {
        Gson gson = new Gson() ;
        FinancialScheduler financialScheduler = financialSchedulerService.findById(id) ;
        Map<String, String> data = gson.fromJson(financialScheduler.getData(), TypeTokenContants.MAP_TOKEN);
        String date = data.get("date");
        switch (financialScheduler.getType()) {
            case "OFFLIME_RECHARGE": // 线下充值同步
                try {
                    assetSynBiz.doOffLineRechargeByAleve(date);
                } catch (Exception e) {
                    log.error("线下充值同步异常", e);
                }
                break;

            case  "ALEVE":
                try {
                    fundStatisticsBiz.doAleve(date);
                } catch (Exception e) {
                    log.error("ALEVE下载异常", e);
                }
                break;
            case  "EVE":
                try {
                    fundStatisticsBiz.doEve(date);
                } catch (Exception e) {
                    log.error("EVE下载异常", e);
                }
                break;
            case  "CURR_INCOME":
                try {
                    currentIncomeLogBiz.process(date);
                } catch (Exception e) {
                    log.error("活期下载异常", e);
                }
                break;
        }

    }
}
