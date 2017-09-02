package com.gofobao.framework.scheduler.biz;

import javax.servlet.http.HttpServletResponse;

public interface FundStatisticsBiz {

    /**
     * eve 下载
     * @param date
     * @return
     * @throws Exception
     */
    boolean doEve(String date) throws Exception;

    /**
     * aleve 下载
     * @return
     * @throws Exception
     * @param date
     */
    boolean doAleve(String date) throws Exception;


    /**
     * 下载对账文件
     * 包含该3个 sheel
     *   1.平台资金流水
     *   2.存管系统资金流水
     *   3.每个人的资金进出| 存管平台的流水
     * @param httpServletResponse
     * @param date
     * @throws Exception
     */
    void downFundFile(HttpServletResponse httpServletResponse, String date) throws Exception ;

}
