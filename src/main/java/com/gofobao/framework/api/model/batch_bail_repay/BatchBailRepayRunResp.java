package com.gofobao.framework.api.model.batch_bail_repay;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/12.
 */
@Data
public class BatchBailRepayRunResp extends JixinBaseResponse{
    /**
     *
     */
    private String batchNo;
    /**
     * 担保电子账户
     */
    private String accountId;
    /**
     *投标的原标的号
     */
    private String productId;
    /**
     *成功金额汇总（请求的txAmount汇总）
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
     * 失败笔数
     */
    private String failCounts;
    /**
     *
     */
    private String acqRes;
    /**
     *
     */
    private String subPacks;

}
