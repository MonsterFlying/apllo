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

@Component
@Slf4j
public class NewAleveAndEveScheduler {

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
    @Scheduled(cron = "0 0 3 * * ?")
    public void process() {
        String date = jixinTxDateHelper.getSubDateStr(1);
        log.info("=======================================");
        log.info(String.format("新版对账系统文件下载, 时间: %s", date));
        log.info("=======================================");

        boolean aleveDownloadState = newAleveBiz.downloadNewAleveFileAndImportDatabase(date); // 下载ALEVE文件下载并且入库
        if (!aleveDownloadState) {
            exceptionEmailHelper.sendErrorMessage("ALEVE文件下载失败",
                    String.format("ALEVE下载失败, 时间: %s", date));
        }

        // 计算活期收益
        try {
            newAleveBiz.calculationCurrentInterest(date);
        } catch (Exception e) {
            log.error("活期处理异常", e);
        }

        boolean eveDownloadState = newEveBiz.downloadEveFileAndSaveDB(date);  // 下载EVE文件并且入库
        if (!eveDownloadState) {
            exceptionEmailHelper.sendErrorMessage("EVE文件下载失败",
                    String.format("EVE下载失败, 时间: %s", date));
        }

        // 针对线下充值进行对账
        try {
            boolean offlineRechargeSysState = offlineRechargeSynBiz.process(date);
            if (!offlineRechargeSysState) {
                exceptionEmailHelper.sendErrorMessage("凌晨3点线下同步失败",
                        String.format("凌晨3点线下同步失败, 时间: %s", date));
            }
        } catch (Exception e) {
            log.error("线下充值记录对账");
        }
    }

}
