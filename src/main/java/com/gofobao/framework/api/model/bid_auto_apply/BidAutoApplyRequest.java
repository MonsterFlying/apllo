package com.gofobao.framework.api.model.bid_auto_apply;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/6/1.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BidAutoApplyRequest extends JixinBaseRequest{
    /**
     * 存管平台分配的账号
     */
    private String accountId;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 交易金额
     */
    private String txAmount;
    /**
     * 标的号
     */
    private String productId;
    /**
     * 是否冻结金额
     */
    private String frzFlag;
    /**
     * 签约订单号
     */
    private String contOrderId;
    /**
     * 请求方保留
     */
    private String acqRes;
}
