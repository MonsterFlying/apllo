package com.gofobao.framework.api.model.credit_invest_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/14.
 */
@Data
public class CreditInvestQueryResp extends JixinBaseResponse {
    /**
     * 投资人电子账号
     */
    private String accountId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 标的号
     */
    private String productId;
    /**
     * 投标金额
     */
    private String txAmount;
    /**
     * 预期收益
     */
    private String forIncome;
    /**
     * 投标日期
     */
    private String buyDate;
    /**
     * 1：投标中；
     * 2：计息中；
     * 4：本息已返还；
     * 9：已撤销；
     */
    private String state;
    /**
     *
     */
    private String authCode;
    /**
     *
     */
    private String bonusAmount;
    /**
     *
     */
    private String acqRes;
}
