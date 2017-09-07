package com.gofobao.framework.api.model.voucher_pay_cancel;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/9/7.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherPayCancelRequest extends JixinBaseRequest {
    /**
     * 原交易的红包账号
     */
    private String accountId;
    /**
     * 原交易的红包金额
     */
    private String txAmount;
    /**
     * 原交易的接收方账号
     */
    private String forAccountId;
    /**
     * 原交易的红包账号
     */
    private String orgTxDate;
    /**
     * 原交易的红包账号
     */
    private String orgTxTime;
    /**
     * 原交易的红包账号
     */
    private String orgSeqNo;
    /**
     * 原交易的红包账号
     */
    private String acqRes;

}
