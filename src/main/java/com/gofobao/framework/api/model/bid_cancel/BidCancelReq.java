package com.gofobao.framework.api.model.bid_cancel;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/6.
 */
@Data
public class BidCancelReq extends JixinBaseRequest {
    /**
     * 电子账号
     */
    private String accountId;
    /**
     * 由P2P生成，必须保证唯一
     */
    private String orderId;
    /**
     * 原投标金额
     */
    private String txAmount;
    /**
     * 原标的号
     */
    private String productId;
    /**
     * 原投标的订单号
     */
    private String orgOrderId;
    /**
     * 请求方保留
     */
    private String acqRes;
}
