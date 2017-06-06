package com.gofobao.framework.api.model.debt_register_cancel;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/5.
 */
@Data
public class DebtRegisterCancelResp extends JixinBaseResponse{
    /**
     * 存管平台分配的借款人电子账号
     */
    private String accountId;
    /**
     * 标的号
     */
    private String productId;
    /**
     * 交易金额 12,2
     */
    private String txAmount;
    /**
     * 姓名
     */
    private String name;
    /**
     * state
     * 1-投标中
     2-计息中
     3-到期待返还
     4-本息已返还
     9-已撤销
     12-标的登记失败
     */
    private String state;
    /**
     * acqRes
     */
    private String acqRes;

}
