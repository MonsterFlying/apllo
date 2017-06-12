package com.gofobao.framework.api.model.account_details_query;

import lombok.Data;

/**
 * Created by Max on 17/6/9.
 */
@Data
public class AccountDetailsQueryItem {
    private String accDate ;
    private String inpDate ;
    private String relDate ;
    private String inpTime ;
    private String traceNo ;
    private String accountId ;
    private String tranType ;
    private String txAmount ;
    private String orFlag ;
    private String describe ;
    private String currency ;
    private String currBal ;
    private String forAccountId ;
}
