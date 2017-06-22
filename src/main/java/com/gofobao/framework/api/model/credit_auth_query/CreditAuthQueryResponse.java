package com.gofobao.framework.api.model.credit_auth_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/6/14.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditAuthQueryResponse extends JixinBaseResponse {
    private String accountId ;
    private String type ;
    private String state ;
    private String orderId ;
    private String txnDate ;
    private String txnTime ;
    private String txAmount ;
    private String totAmount ;
}
