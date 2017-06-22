package com.gofobao.framework.api.model.direct_recharge_plus;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
public class DirectRechargePlusResponse extends JixinBaseResponse {
    private String accountId;

    private String orderId ;

    private String acqRes;

    private String mobile ;

    private String txAmount ;
}

