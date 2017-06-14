package com.gofobao.framework.api.model.bid_apply_query;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/14.
 */
@Data
public class BidApplyQueryReq extends JixinBaseRequest {
    /**
     * 投资人电子账号
     */
    private String accountId;
    /**
     * 原订单号
     */
    private String orgOrderId;
    /**
     *
     */
    private String acqRes;
}
