package com.gofobao.framework.api.model.balance_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/5/16.
 */
@Data
public class BalanceQueryResponse extends JixinBaseResponse {
    /**
     * 电子账号
     */
    private String accountId;
    private String name;

    /**
     * 0-基金账户  1-靠档计息账户 2-活期账户
     */
    private String accType;
    /**
     *  账户用户
     */
    private String acctUse;

    /**
     *  可用余额
     */
    private String availBal;

    /**
     *  账面余额
     */
    private String currBal;

    /**
     * 提现开关
     */
    private String withdrawFlag;
}
