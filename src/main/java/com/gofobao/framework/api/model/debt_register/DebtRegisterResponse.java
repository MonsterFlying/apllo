package com.gofobao.framework.api.model.debt_register;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/6/1.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebtRegisterResponse extends JixinBaseResponse {
    /**
     * 电子账号
     */
    private String accountId;
    /**
     * 标的id
     */
    private String productId;
    /**
     * 标的描述
     */
    private String productDesc;
    /**
     * 交易金额 12,2 借款金额
     */
    private String txAmount;
    /**
     * 姓名
     */
    private String name;
    /**
     * 名义借款人电子账号
     */
    private String nominalAcctountId;

    /**
     * 收款人电子帐户
     */
    private String receiptAccountId;

    /**
     * 请求方保留
     */
    private String acqRes;
}
