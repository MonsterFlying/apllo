package com.gofobao.framework.api.model.batch_credit_end;

import lombok.Data;

/**
 * Created by Zeke on 2017/7/19.
 */
@Data
public class CreditEnd {
    /**
     * 融资人电子账号
     */
    private String accountId;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 投资人账号
     */
    private String forAccountId;
    /**
     * 标的号
     */
    private String productId;
    /**
     * 投资人投标成功的授权号
     */
    private String authCode;
}
