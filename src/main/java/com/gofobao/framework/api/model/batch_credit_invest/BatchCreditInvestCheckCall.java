package com.gofobao.framework.api.model.batch_credit_invest;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/7.
 */
@Data
public class BatchCreditInvestCheckCall extends JixinBaseResponse{
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 交易金额
     */
    private String txAmount;
    /**
     * 交易笔数
     */
    private String txCounts;
    /**
     * 请求方保留
     */
    private String acqRes;
}
