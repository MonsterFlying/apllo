package com.gofobao.framework.scheduler.biz;

public interface FundStatisticsBiz {

    /**
     * eve 下载
     * @param date
     * @return
     * @throws Exception
     */
    boolean doEve(String date) throws Exception;

    /**
     *
     * @return
     * @throws Exception
     */
    boolean doAleve() throws Exception;
}
