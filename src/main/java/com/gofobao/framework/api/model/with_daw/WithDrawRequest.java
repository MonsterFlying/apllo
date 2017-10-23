package com.gofobao.framework.api.model.with_daw;

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
public class WithDrawRequest extends JixinBaseRequest {
    private String accountId ;
    private String idType ;
    private String idNo ;
    private String name ;
    private String mobile ;
    private String cardNo ;
    private String txAmount ;
    private String txFee ;
    private String routeCode ;
    private String cardBankCnaps ;
    private String cardBankCode ;
    private String cardBankNameCn ;
    private String cardBankNameEn ;
    private String cardBankProvince ;
    private String cardBankCity ;
    private String forgotPwdUrl ;
    private String businessAccountIdFlag ;
    private String retUrl ;
    private String notifyUrl ;
    private String acqRes ;
}
