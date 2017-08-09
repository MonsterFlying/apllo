package com.gofobao.framework.api.model.batch_repay_bail;

import lombok.Data;

/**
 * Created by Zeke on 2017/6/12.
 */
@Data
public class RepayBail {
    /**
     *  账户名
     */
    private String accountId;
    /**
     *  订单号
     */
    private String orderId;
    /**
     * 融资人实际付出金额=交易金额+交易利息+还款手续费
     */
    private String txAmount;
    /**
     * 利息
     */
    private String intAmount;
    /**
     * 向融资人收取的手续费
     */
    private String txFeeOut;
    /**
     * 担保账号
     */
    private String forAccountId;
    /**
     * 担保账户垫付时的原订单号
     */
    private String orgOrderId;
    /**
     * 担保账户获得的债权授权码
     */
    private String authCode;
}
