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
    int checkBatchOftenSubmit(String sourceId,Integer ... type);

    /**
     * 校验本地资源回调状态
     * @param sourceId
     * @param type
     * @return true 已处理  false 未处理
     */
    boolean checkLocalSourceState(String sourceId,int type);
}
