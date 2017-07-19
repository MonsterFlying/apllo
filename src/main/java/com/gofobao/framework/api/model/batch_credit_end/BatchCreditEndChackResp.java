package com.gofobao.framework.api.model.batch_credit_end;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/19.
 */
@Data
public class BatchCreditEndChackResp extends JixinBaseResponse{
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 交易笔数
     */
    private String txCounts;
    /**
     * 请求方保留
     */
    private String acqRes;
}
