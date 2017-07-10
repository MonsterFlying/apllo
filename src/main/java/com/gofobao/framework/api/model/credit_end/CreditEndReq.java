package com.gofobao.framework.api.model.credit_end;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/7/10.
 */
@Data
public class CreditEndReq extends JixinBaseRequest {
    /**
     * 融资人电子账号
     */
    private String accountId;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 投资人账号
     */
    private String forAccountId;
    /**
     * 投资人投标成功的标的号
     */
    private String productId;
    /**
     *  投资人投标成功的授权号
     */
    private String authCode;
    /**
     *
     */
    private String acqRes;

}
