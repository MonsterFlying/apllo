package com.gofobao.framework.api.model.batch_credit_end;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/19.
 */
@Data
public class BatchCreditEndRunResp extends JixinBaseResponse{
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 成功交易笔数
     */
    private String sucCounts;
    /**
     * 失败交易笔数
     */
    private String failCounts;
    /**
     * 请求方保留
     */
    private String acqRes;
}
