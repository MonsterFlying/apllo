package com.gofobao.framework.api.model.red_package_send;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by admin on 2017/6/28.
 */
@Data
public class RedPackageSendResponse  extends JixinBaseResponse {

    /**
     * 用户电子账号
     */
    private String accountId;
    /**
     * 交易金额
     */
    private String txAccount;
    /**
     * 可选字段
     */
    private String acqRes;
}
