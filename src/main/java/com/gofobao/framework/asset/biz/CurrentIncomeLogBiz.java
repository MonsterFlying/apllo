package com.gofobao.framework.asset.biz;

public interface CurrentIncomeLogBiz {

    /**
     * 调度处理活期收益
     * @return
     * @param date
     */
    boolean process(String date) throws Exception;


}
