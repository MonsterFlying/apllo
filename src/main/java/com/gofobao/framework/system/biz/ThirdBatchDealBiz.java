package com.gofobao.framework.system.biz;

/**
 * Created by Zeke on 2017/9/14.
 */
public interface ThirdBatchDealBiz {

    /**
     * 批次处理
     *
     * @param sourceId
     * @param batchNo
     * @param acqRes
     * @param batchResp
     * @return
     * @throws Exception
     */
    boolean batchDeal(long sourceId, String batchNo, String acqRes, String batchResp) throws Exception;


}
