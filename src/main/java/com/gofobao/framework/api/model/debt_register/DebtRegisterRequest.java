package com.gofobao.framework.api.model.debt_register;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Zeke on 2017/6/1.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebtRegisterRequest extends JixinBaseRequest {
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
     * 募集日 YYYYMMDD
     */
    private String raiseDate;
    /**
     * 募集结束日期  YYYYMMDD
     * 募集期不得超过规定工作日，超过结束日期未能满标，标的失效，不能再投标或满标，但需要P2P主动发起撤销所有
     */
    private String raiseEndDate;
    /**
     * 付息方式 0-到期与本金一起归还
     * 1-每月固定日期支付
     * 2-每月不确定日期支付
     */
    private String intType;
    /**
     * 利息每月支付日 DD 选填
     * 付息方式为1时必填；
     * 若设置日期大于月份最后一天时，则为该月最后一天支付
     * 平台仅记录
     */
    private String intPayDay;
    /**
     * 借款期限 天数，从满标日期开始计算
     */
    private String duration;
    /**
     * 交易金额 12,2 借款金额
     */
    private String txAmount;
    /**
     * 年化利率 8,5 百分数
     */
    private String rate;
    /**
     * 平台手续费  12,2 平台收取的总手续费 选填
     */
    private String txFee;
    /**
     * 担保电子账号 选填
     */
    private String bailAccountId;
    /**
     * 名义借款人电子账号 选填
     */
    private String nominalAccountId;
    /**
     * 请求方保留 选填
     */
    private String acqRes;
    /**
     * 当entrustFlag不为空时必填
     */
    private String receiptAccountId;
    /**
     * 为空时单一借款人模式
     * 1：受托支付业务类别
     */
    private String entrustFlag;
}
