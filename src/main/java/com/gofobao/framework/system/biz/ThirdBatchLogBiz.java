package com.gofobao.framework.system.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.entity.ThirdBatchLog;
import com.gofobao.framework.system.vo.request.VoFindThirdBatch;
import com.gofobao.framework.system.vo.request.VoSendThirdBatch;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/7/14.
 */
public interface ThirdBatchLogBiz {

    /**
     * 查询批次状态
     *
     * @return
     */
    ResponseEntity<VoBaseResp> findThirdThirdBatch(VoFindThirdBatch voFindThirdBatch);

    /**
     * 发送即信批次处理
     *
     * @param voSendThirdBatch
     * @return
     */
    ResponseEntity<VoBaseResp> sendThirdBatchDeal(VoSendThirdBatch voSendThirdBatch);

    /**
     * 更新批次日志状态
     *
     * @param batchNo
     * @return
     */
    boolean updateBatchLogState(String batchNo, Long sourceId, int state, int type);

    /**
     * 更据sourceId检查批次是否频繁提交
     *
     * @param sourceId
     * @return
     */
    int checkBatchOftenSubmit(String sourceId, Integer... type);

    /**
     * 校验本地资源回调状态
     *
     * @param sourceId
     * @param type
     * @return true 已处理  false 未处理
     */
    boolean checkLocalSourceState(String sourceId, int type);

    /**
     * 获取有效的最后一条批次记录
     *
     * @param sourceId
     * @param type
     * @return
     */
    ThirdBatchLog getValidLastBatchLog(String sourceId, Integer... type);

}
