package com.gofobao.framework.api.model.bid_auto_apply;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/6/1.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BidAutoApplyResponse extends JixinBaseResponse{
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
     * 授权码
     */
    private String authCode;
    /**
     * 请求方保留
     */
    private String acqRes;
}
