package com.gofobao.framework.api.model.offline_recharge_call;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/23.
 */
@Data
public class OfflineRechargeCallResponse extends JixinBaseResponse {
    /**
     * 电子账号
     */
    private String accountId ;

    /**
     * 请求方保留
     */
    private String acqRes ;
}
