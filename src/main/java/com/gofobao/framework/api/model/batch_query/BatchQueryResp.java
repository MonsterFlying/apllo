package com.gofobao.framework.api.model.batch_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/18.
 */
@Data
public class BatchQueryResp extends JixinBaseResponse{
    /**
     * 批次交易日期
     */
    private String batchTxDate;
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 批次交易代码
     */
    private String batchTxCode;
    /**
     * 批次处理金额
     */
    private String relAmount;
    /**
     * 批次处理笔数
     */
    private String relCounts;
    /**
     * 批次处理状态
     * 待处理    --A
     处理中    --D
     处理结束  --S
     处理失败  --F
     已撤销    --C
     */
    private String batchState;
    /**
     * 当batchState=F时必填
     */
    private String failMsg;
    /**
     * 成功笔数
     */
    private String sucCounts;
    /**
     * 成功金额
     */
    private String sucAmount;
    /**
     * 失败笔数
     */
    private String failCounts;
    /**
     * 失败金额
     */
    private String failAmount;
}
