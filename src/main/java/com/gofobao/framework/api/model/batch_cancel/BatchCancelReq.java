package com.gofobao.framework.api.model.batch_cancel;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/27.
 */
@Data
public class BatchCancelReq extends JixinBaseRequest {
    /**
     * 需要撤销的批次号
     */
    private String batchNo;
    /**
     * 本批次所有交易金额汇总
     */
    private String txAmount;
    /**
     * 本批次所有交易笔数
     */
    private String txCounts;
    /**
     *
     */
    private String acqRes;
}
