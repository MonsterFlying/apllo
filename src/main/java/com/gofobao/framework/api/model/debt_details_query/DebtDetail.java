package com.gofobao.framework.api.model.debt_details_query;

import lombok.Data;

/**
 * Created by Zeke on 2017/6/5.
 */
@Data
public class DebtDetail {
    /**
     * 标的号
     */
    private String productId;
    /**
     * 募集日 YYYYMMDD
     */
    private String raiseDate;
    /**
     * 募集结束日期
     * YYYYMMDD
     募集期不得超过规定工作日，超过结束日期未能满标，标的失效，不能再投标或满标，但需要P2P主动发起撤销所有投标
     */
    private String raiseEndDate;
    /**
     * 付息方式
     * 0-到期与本金一起归还
     1-每月固定日期支付
     2-每月不确定日期支付
     平台仅记录
     */
    private String intType;
    /**
     * 利息每月支付日  DD
     付息方式为1时必填；
     若设置日期大于月份最后一天时，则为该月最后一天支付
     平台仅记录
     */
    private String intPayDay;
    /**
     * 借款期限
     */
    private String duration;
    /**
     * 交易金额
     */
    private String txAmount;
    /**
     * 年化利率 8,5 百分数
     */
    private String rate;
    /**
     * 平台收取的总手续费
     */
    private String fee;
    /**
     * 担保电子账号
     */
    private String bailaccountId;
    /**
     * 起息日 YYYYMMDD
     */
    private String intDate;
    /**
     * 募集总金额
     */
    private String raiseAmount;
    /**
     * 标的已还本金
     */
    private String repaymentAmt;
    /**
     * 标的已还利息
     */
    private String repaymentInt;
    /**
     * 1-投标中
     2-计息中
     3-到期待返还
     4-本息已返还
     8-审核中
     9-已撤销
     */
    private String state;
}
