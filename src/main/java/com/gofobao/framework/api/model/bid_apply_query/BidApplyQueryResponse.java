package com.gofobao.framework.api.model.bid_apply_query;

import com.gofobao.framework.api.repsonse.JixinBaseResponse;
import lombok.Data;

/**
 * Created by Zeke on 2017/6/14.
 */
@Data
public class BidApplyQueryResponse extends JixinBaseResponse {
    /**
     * 买入方账号
     */
    private String accountId;
    /**
     * 买入方姓名
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
     * 状态
     * 1：投标中；
     2：计息中；
     4：本息已返还；
     9：已撤销；
     */
    private String state;
    /**
     * 授权码
     */
    private String authCode;
    /**
     * 抵扣红包金额
     */
    private String bonusAmount;
    /**
     * 请求方保留
     */
    private String acqRes;
}
