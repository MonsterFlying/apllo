package com.gofobao.framework.api.model.direct_recharge_plus.auto_credit_invest_auth_plus;

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
public class DirectRechargePlusRequest extends JixinBaseRequest {
    private String accountId ;
    private String idType ;
    private String idNo ;
    private String name ;
    private String mobile ;
    private String cardNo ;
    private String txAmount ;
    private String currency ;
    private String cardBankCode;
    private String cardBankNameCn ;
    private String cardBankNameEn ;
    private String cardBankProvince;
    private String cardBankCity;
    private String retUrl;
    private String notifyUrl;
    private String userIP;
    private String lastSrvAuthCode;
    private String smsCode;
    private String acqRes;
}
