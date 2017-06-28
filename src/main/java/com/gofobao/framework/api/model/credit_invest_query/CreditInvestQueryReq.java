package com.gofobao.framework.api.model.credit_invest_query;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/14.
 */
@Data
public class CreditInvestQueryReq extends JixinBaseRequest {
    /**
     *投资人电子账号
     */
    private String accountId;
    /**
     * 原投标订单号
     */
    private String orgOrderId;
    /**
     *
     */
    private String acqRes;

}
