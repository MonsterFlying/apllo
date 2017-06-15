package com.gofobao.framework.api.model.batch_repay_bail;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/12.
 */
@Data
public class BatchRepayBailReq extends JixinBaseRequest {
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 本批次所有交易金额汇总
     */
    private String txAmount;
    /**
     * 本批次所有交易笔数
     */
    private String txCounts;
    /**
     * 后台通知URL，“响应参数”返回到该URL，收到后返回“success”
     * 对数据合法性检查等的通知
     */
    private String notifyURL;
    /**
     * 后台通知URL，“响应参数”返回到该URL，收到后返回“success”
     * 对请求业务处理的结果通知
     */
    private String retNotifyURL;
    /**
     *
     */
    private String acqRes;
    /**
     *
     */
    private String subPacks;
}
