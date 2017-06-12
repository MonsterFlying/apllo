package com.gofobao.framework.api.model.batch_repay_bail;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/12.
 */
@Data
public class BatchRepayBailRunResp extends JixinBaseResponse {
    /**
     *
     */
    private String batchNo;
    /**
     * 成功金额汇总（请求的txAmount汇总）
     */
    private String sucAmount;
    /**
     * 成功笔数
     */
    private String sucCounts;
    /**
     * 失败金额汇总（请求的txAmount汇总）
     */
    private String failAmount;
    /**
     * 失败交易笔数
     */
    private String failCounts;
    /**
     *
     */
    private String acqRes;
}
