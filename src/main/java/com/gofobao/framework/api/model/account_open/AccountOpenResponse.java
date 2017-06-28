package com.gofobao.framework.api.model.account_open;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
public class AccountOpenResponse extends JixinBaseResponse {
    /**
     * 电子账号
     */
    private String accountId;
    /**
     * 请求方保留
     */
    private String acqRes;
}
