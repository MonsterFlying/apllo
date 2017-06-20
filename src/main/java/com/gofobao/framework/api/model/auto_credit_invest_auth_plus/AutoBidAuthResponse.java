package com.gofobao.framework.api.model.auto_credit_invest_auth_plus;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
public class AutoBidAuthResponse extends JixinBaseResponse {
    private String accountId;

    private String orderId ;

    private String acqRes;

    private String name ;

    private String txAmount ;
}

