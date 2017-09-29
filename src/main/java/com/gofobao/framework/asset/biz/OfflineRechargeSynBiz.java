package com.gofobao.framework.asset.biz;

/**
 * 线下充值
 */
public interface OfflineRechargeSynBiz {


    /**
     * 线下充值同步(俗称捡漏)
     * @param date
     * @return
     */
    boolean process(String date);
}
