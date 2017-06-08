package com.gofobao.framework.api.model.batch_repay;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/8.
 */
@Data
public class BatchRepayRunResp extends JixinBaseResponse {
    /**
     * 当日相同的批次号交易后台在一个批量中处理
     批次号当日必须唯一 6位
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
     * 请求方保留
     */
    private String acqRes;
}
