package com.gofobao.framework.api.model.trustee_pay_query;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/21.
 */
@Data
public class TrusteePayQueryReq extends JixinBaseRequest {
    /**
     * 电子账号
     */
    private String accountId;
    /**
     * 标的编号
     */
    private String productId;
}
