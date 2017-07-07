package com.gofobao.framework.api.model.bid_cancel;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/6.
 */
@Data
public class BidCancelResp extends JixinBaseResponse {
    /**
     * 电子账号
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
     * 姓名
     */
    private String name;
    /**
     * 请求方保留
     */
    private String acqRes;
}
