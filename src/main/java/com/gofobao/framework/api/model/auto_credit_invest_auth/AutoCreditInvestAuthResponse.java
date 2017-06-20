package com.gofobao.framework.api.model.auto_credit_invest_auth;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
public class AutoCreditInvestAuthResponse extends JixinBaseResponse {
    /**
     * 电子账号
     */
    private String accountId;

    /**
     * 订单号
     */
    private String orderId ;
    /**
     * 请求方保留
     */
    private String acqRes;
}
