package com.gofobao.framework.api.model.auto_bid_auth_plus.auto_credit_invest_auth_plus;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutoCreditInvestAuthPlusRequest extends JixinBaseRequest {
    private String accountId ;

    private String orderId ;

    private String forgotPwdUrl ;

    private String retUrl ;

    private String notifyUrl;

    private String lastSrvAuthCode ;

    private String smsCode ;

    private String acqRes;
}
