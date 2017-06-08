package com.gofobao.framework.api.model.batch_credit_invest;

import lombok.Data;

/**
 * Created by Zeke on 2017/6/7.
 */
@Data
public class CreditInvest {
    /**
     * 买入方账号
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
     * 手续费 选填
     */
    private String txFee;
    /**
     * 转让金额
     */
    private String tsfAmount;
    /**
     * 对手电子账号
     */
    private String forAccountId;
    /**
     * 原订单号
     */
    private String orgOrderId;
    /**
     * 原交易金额 卖出方投标的原交易金额
     （或卖出方购买债权的原交易金额）
     */
    private String orgTxAmount;
    /**
     * 标的号
     */
    private String productId;
    /**
     * 签约订单号
     */
    private String contOrderId;
}
