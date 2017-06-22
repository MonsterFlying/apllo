package com.gofobao.framework.api.model.direct_recharge_online;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Administrator on 2017/6/21 0021.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectRechargeOnlineRequest extends JixinBaseRequest {
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
    private String callBackAddress;
    private String userIP;
    private String smsSeq;
    private String smsCode;
    private String acqRes;
}
