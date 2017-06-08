package com.gofobao.framework.api.model.batch_credit_invest;

import lombok.Data;

/**
 * Created by Zeke on 2017/6/7.
 */
@Data
public class CreditInvestRun {
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
     * 卖出方原标的号
     */
    private String productId;
    /**
     * 交易成功生成的买入方授权码
     */
    private String authCode;
}
