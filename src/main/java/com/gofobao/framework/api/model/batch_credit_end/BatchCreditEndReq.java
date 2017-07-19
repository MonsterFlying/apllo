package com.gofobao.framework.api.model.batch_credit_end;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/19.
 */
@Data
public class BatchCreditEndReq extends JixinBaseRequest{
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 交易笔数
     */
    private String txCounts;
    /**
     * 后台通知链接
     */
    private String notifyURL;
    /**
     * 业务结果通知
     */
    private String retNotifyURL;
    /**
     * 请求方保留
     */
    private String acqRes;
    /**
     * 请求数组
     */
    private String subPacks;
}
