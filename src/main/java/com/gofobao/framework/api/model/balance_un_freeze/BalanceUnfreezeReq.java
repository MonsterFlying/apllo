package com.gofobao.framework.api.model.balance_un_freeze;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/6.
 */
@Data
public class BalanceUnfreezeReq extends JixinBaseRequest {
    /**
     * 电子账号
     */
    private String accountId;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 原冻结金额
     */
    private String txAmount;
    /**
     * 原冻结订单号
     */
    private String orgOrderId;
    /**
     *
     */
    private String acqRes;
}
