package com.gofobao.framework.api.model.voucher_pay_cancel;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/9/7.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherPayCancelResponse extends JixinBaseResponse {
    /**
     * 原交易的红包账号
     */
    private String accountId;
    /**
     * 交易金额
     */
    private String txAmount;
    /**
     * 请求方保留
     */
    private String acqRes;
}
