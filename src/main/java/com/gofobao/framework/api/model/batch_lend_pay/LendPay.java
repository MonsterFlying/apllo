package com.gofobao.framework.api.model.batch_lend_pay;

import lombok.Data;

/**
 * Created by Zeke on 2017/6/8.
 */
@Data
public class LendPay {
    /**
     * 融资人电子账号
     */
    private String accountId;
    /**
     * 由P2P生成，必须保证唯一
     */
    private String orderId;
    /**
     * 投资人实际付出金额=交易金额+投资手续费 12,2
     */
    private String txAmount;
    /**
     * 投资手续费
     */
    private String bidFee;
    /**
     * 融资手续费
     */
    private String debtFee;
    /**
     * 融资人账号
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
