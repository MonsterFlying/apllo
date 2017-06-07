package com.gofobao.framework.api.model.batch_credit_invest;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/7.
 */
@Data
public class BatchCreditInvestReq extends JixinBaseRequest {
    /**
     * 本批次所有交易金额汇总
     */
    private String batchNo;
    /**
     * 本批次所有交易笔数
     */
    private String txAmount;
    /**
     * 后台通知URL，“响应参数”返回到该URL，收到后返回“success”
     对数据合法性检查等的通知
     */
    private String notifyURL;
    /**
     * 后台通知URL，“响应参数”返回到该URL，收到后返回“success”
     对请求业务处理的结果通知
     */
    private String retNotifyURL;
    /**
     * 请求方保留 选填
     */
    private String acqRes;
    /**
     * 请求数组 选填
     */
    private String subPacks;
}
