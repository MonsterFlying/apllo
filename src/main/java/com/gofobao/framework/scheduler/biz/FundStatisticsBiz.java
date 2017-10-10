package com.gofobao.framework.scheduler.biz;

import javax.servlet.http.HttpServletRequest;
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


}
