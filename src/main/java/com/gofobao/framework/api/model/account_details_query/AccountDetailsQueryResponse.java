package com.gofobao.framework.api.model.account_details_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
public class AccountDetailsQueryResponse extends JixinBaseResponse {
    private String accountId ;
    private String startDate ;
    private String endDate ;
    private String type ;
    private String name ;
    private String pageNum ;
    private String pageSize ;
    private String totalItems ;
    private String subPacks ;
    private String accDate ;
    private String inpDate ;
    private String relDate ;
    private String inpTime ;
    private String traceNo ;
    private String tranType ;
    private String orFlag ;
    private String describe ;
    private String currency ;
    private String currBal ;
    private String forAccountId ;
}
