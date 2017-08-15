package com.gofobao.framework.api.model.batch_details_query;

import lombok.Data;

/**
 * Created by Zeke on 2017/6/14.
 */
@Data
public class DetailsQueryResp {
    /**
     * 电子账号
     */
    private String accountId;
    /**
     *
     */
    private String orderId;
    /**
     * 交易金额
     */
    private String txAmount;
    /**
     * 对手电子账号
     */
    private String forAccountId;
    /**
     *  标的号
     */
    private String productId;
    /**
     * 授权码 批次交易代码为批次投资人购买债权、批次名义借款人垫付时，该字段代表交易成功生成的买入方授权码
     */
    private String authCode;
    /**
     * 交易状态 S-成功
     F-失败
     D-待处理
     */
    private String txState;
    /**
     * txState为F时有效
     */
    private String failMsg;
}
