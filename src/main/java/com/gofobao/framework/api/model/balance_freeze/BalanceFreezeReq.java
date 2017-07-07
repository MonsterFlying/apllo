package com.gofobao.framework.api.model.balance_freeze;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/6.
 */
@Data
public class BalanceFreezeReq extends JixinBaseRequest {
    /**
     * 电子账号
     */
    private String accountId;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 冻结金额
     */
    private String txAmount;
    /**
     * 请求方保留
     */
    private String acqRes;
}
