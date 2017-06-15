package com.gofobao.framework.api.model.trustee_pay;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/12.
 */
@Data
public class TrusteePayResp extends JixinBaseResponse{
    /**
     *
     */
    private String accountId;
    /**
     * 标的编号
     */
    private String productId;
    /**
     * 收款方电子账号
     */
    private String receiptAccountId;
    /**
     * 记录状态 0：未确认
     1：已确认
     */
    private String state;
    /**
     *
     */
    private String acqRes;

}
