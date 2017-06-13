package com.gofobao.framework.api.model.batch_bail_repay;

import lombok.Data;

/**
 * Created by Zeke on 2017/6/12.
 */
@Data
public class BailRepayRun {
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 交易金额 垫付金额=交易本金+交易利息
     */
    private String txAmount;
    /**
     * 垫付本金
     */
    private String txCapAmout;
    /**
     * 垫付利息
     */
    private String txIntAmount;
    /**
     * 投资人电子账户
     */
    private String forAccountId;
    /**
     * 交易成功生成的买入方授权码
     */
    private String authCode;
}
