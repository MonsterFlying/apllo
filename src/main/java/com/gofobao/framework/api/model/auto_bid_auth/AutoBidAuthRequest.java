package com.gofobao.framework.api.model.auto_bid_auth;

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
public class AutoBidAuthRequest extends JixinBaseRequest {
    private String accountId ;

    private String orderId ;

    private String txAmount ;

    private String totAmount ;

    private String forgotPwdUrl ;

    private String retUrl ;

    private String notifyUrl;

    private String acqRes;
}
