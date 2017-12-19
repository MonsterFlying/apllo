package com.gofobao.framework.api.model.credit_invest;

import com.gofobao.framework.api.request.JixinBaseRequest;
import lombok.Data;

/**
 * Created by Zeke on 2017/12/19.
 */
@Data
public class CreditInvestRequest extends JixinBaseRequest {
    /**
     * 购买方账号
     */
    private String accountId;
    /**
     * 订单号由P2P生成，使用字母+数字的组合，不区分大小写，且必须保证唯一；
     */
    private String orderId;
    /**
     * 成交价格，购买方实际付出金额
     */
    private String txAmount;
    /**
     * 向卖出方收取的手续费
     */
    private String txFee;
    /**
     * 卖出的债权金额
     */
    private String tsfAmount;
    /**
     * 卖出方账号
     */
    private String forAccountId;
    /**
     * 卖出方投标的原订单号
     * （或卖出方购买债权的原订单号）
     */
    private String orgOrderId;
    /**
     * 卖出方投标的原交易金额
     * （或卖出方购买债权的原转让金额）
     */
    private String orgTxAmount;
    /**
     * 原标的号；标的号不区分大小写；
     */
    private String productId;
    /**
     *
     */
    private String remark;
    /**
     * 忘记密码的跳转URL
     */
    private String forgotPwdUrl;
    /**
     * 交易后台跳转的前台URL
     */
    private String retUrl;
    /**
     * 【响应参数】会返回到该URL，平台收到后请返回“success”
     */
    private String notifyUrl;
    /**
     */
    private String acqRes;
}
