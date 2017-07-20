package com.gofobao.framework.api.model.voucher_pay;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/5/24.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherPayRequest extends JixinBaseRequest{
    /**
     * 电子账号
     */
    private String accountId;
    /**
     * 交易金额 红包金额  12,2
     */
    private String txAmount;
    /**
     * 对手电子账号  接收方账号
     */
    private String forAccountId;
    /**
     * 是否使用交易描述 1-使用 0-不使用
     */
    private String desLineFlag;
    /**
     * 交易描述 选填
     */
    private String desLine;
    /**
     * 请求方保留 选填
     */
    private String acqRes;

}
