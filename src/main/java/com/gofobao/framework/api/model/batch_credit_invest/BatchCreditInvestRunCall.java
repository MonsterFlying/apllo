package com.gofobao.framework.api.model.batch_credit_invest;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/7.
 */
@Data
public class BatchCreditInvestRunCall extends JixinBaseResponse {
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 成功金额汇总（请求的txAmount汇总）
     */
    private String sucAmount;
    /**
     *  成功交易笔数
     */
    private String sucCounts;
    /**
     * 失败交易金额
     */
    private String failAmount;
    /**
     * 失败交易笔数
     */
    private String failCounts;
    /**
     * 请求方保留
     */
    private String acqRes;
    /**
     * 结果数组
     */
    private String subPacks;
}
