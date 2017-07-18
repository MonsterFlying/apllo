package com.gofobao.framework.system.biz;

/**
 * Created by Zeke on 2017/7/14.
 */
public interface ThirdBatchLogBiz {

    /**
     * 更新批次日志状态
     * @param batchNo
     * @return
     */
    boolean updateBatchLogState(String batchNo,Long sourceId,int state);

    /**
     * 更据sourceId检查批次是否频繁提交
     * @param sourceId
     * @return
     */
    boolean checkBatchOftenSubmit(String sourceId,int ... type);
}
