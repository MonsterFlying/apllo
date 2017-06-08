package com.gofobao.framework.api.model.batch_repay;

import lombok.Data;

/**
 * Created by Zeke on 2017/6/8.
 */
@Data
public class Repay {
    /**
     * 融资人电子账号
     */
    private String accountId;
    /**
     * 由P2P生成，必须保证唯一
     */
    private String orderId;
    /**
     * 融资人实际付出金额=交易金额+交易利息+还款手续费 12,2
     */
    private String txAmount;
    /**
     * 交易利息
     */
    private String intAmount;
    /**
     * 向融资人收取的手续费
     */
    private String txFeeOut;
    /**
     * 向投资人收取的手续费
     */
    private String txFeeIn;
    /**
     * 对手电子账号
     */
    private String forAccountId;
    /**
     * 投资人投标成功的标的号
     */
    private String productId;
    /**
     * 投资人投标成功的授权号
     */
    private String authCode;
}
