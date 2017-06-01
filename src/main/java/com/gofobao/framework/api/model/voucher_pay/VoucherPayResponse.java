package com.gofobao.framework.api.model.voucher_pay;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/5/24.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherPayResponse extends JixinBaseResponse{
    /**
     * 电子账号
     */
    private String accountId;
    /**
     * 交易金额 红包金额  12,2
     */
    private String txAmount;
    /**
     * 请求方保留 选填
     */
    private String acqRes;
}
