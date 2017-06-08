package com.gofobao.framework.api.model.batch_repay;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/8.
 */
@Data
public class BatchRepayReq extends JixinBaseRequest {
    /**
     * 当日相同的批次号交易后台在一个批量中处理
     批次号当日必须唯一 6位
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
     * JSON数组，内容解释见下文  选填
     */
    private String subPacks;
}
