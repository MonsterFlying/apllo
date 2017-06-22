package com.gofobao.framework.api.model.direct_recharge_online;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
public class DirectRechargeOnlineResponse extends JixinBaseResponse {
    private String accountId;

    private String orderId ;

    private String acqRes;

    private String currency ;

    private String txAmount ;
}

