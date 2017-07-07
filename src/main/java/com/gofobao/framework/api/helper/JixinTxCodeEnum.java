package com.gofobao.framework.api.helper;

/**
 * 即信交易类型
 * Created by Max on 17/5/19.
 */

public enum JixinTxCodeEnum {
    /**
     * 开户增强
     */
    OPEN_ACCOUNT_PLUS(
            "accountOpenPlus",
            "/escrow/p2p/online"),


    /**
     * 开户
     */
    OPEN_ACCOUNT(
            "accountOpen",
            "/escrow/p2p/page/mobile"),

    /**
     * 查询用户资产
     */
    BALANCE_QUERY(
            "balanceQuery",
            "/escrow/p2p/online"),

    /**
     * 查询签约状态
     */
    CREDIT_AUTH_QUERY(
            "creditAuthQuery",
            "/escrow/p2p/online"),

    /**
     * 提现
     */
    WITH_DRAW(
            "withdraw",
            "/escrow/p2p/page/withdraw"),


    /**
     * 发送短信验证码
     */
    SMS_CODE_APPLY(
            "smsCodeApply",
            "/escrow/p2p/online"),


    BID_AUTO_APPLY(
            "bidAutoApply",
            "/escrow/p2p/online"
    ),

    /**
     * 初始化密码
     */
    PASSWORD_SET(
            "passwordSet",
            "/escrow/p2p/page/passwordset"),

    /**
     * 充值密码
     */
    PASSWORD_RESET(
            "passwordReset",
            "/escrow/p2p/page/mobile"),

    /**
     * 红包发放
     */
    SEND_RED_PACKET(
            "voucherPay",
            "/escrow/p2p/online"),

    /**
     * 标的登记
     */
    DEBT_REGISTER(
            "debtRegister",
            "/escrow/p2p/online"),


    /**
     * 自动投标增强
     */
    AUTO_BID_AUTH_PLUS(
            "autoBidAuthPlus",
            "/escrow/p2p/page/mobile/plus"),

    /**
     * 自动投标
     */
    AUTO_BID_AUTH(
            "autoBidAuth",
            "/escrow/p2p/page/mobile"),
    /**
     * 债权转让增强
     */
    AUTO_CREDIT_INVEST_AUTH_PLUS(
            "autoCreditInvestAuthPlus",
            "/escrow/p2p/page/mobile/plus"),
    /**
     * 债权转让
     */
    AUTO_CREDIT_INVEST_AUTH(
            "autoCreditInvestAuth",
            "/escrow/p2p/page/mobile"),


    /**
     * 借款人标的撤销
     */
    DEBT_REGISTER_CANCEL(
            "debtRegisterCancel",
            "/escrow/p2p/online"),

    /**
     * 充值
     */
    DIRECT_RECHARGE_PLUS(
            "directRechargePlus",
            "/escrow/p2p/page/mobile/plus"),


    /**
     * 联机充值
     */
    DIRECT_RECHARGE_ONLINE(
            "directRechargeOnline",
            "/escrow/p2p/online"),

    /**
     * 借款人标的信息查询
     */
    DEBT_DETAILS_QUERY(
            "debtDetailsQuery",
            "/escrow/p2p/online"),
    /**
     * 批次投资人购买债权
     */
    BATCH_CREDIT_INVEST(
            "batchCreditInvest",
            "/escrow/p2p/online"
    ),
    /**
     * 批次还款
     */
    BATCH_REPAY(
            "batchRepay",
            "/escrow/p2p/online"
    ),
    /**
     * 批次放款
     */
    BATCH_LEND_REPAY(
            "batchLendPay",
            "/escrow/p2p/online"
    ),
    /**
     * 查询用户资金
     */
    ACCOUNT_DETAILS_QUERY(
            "accountDetailsQuery",
            "/escrow/p2p/online"
    ),
    /**
     * 批次融资人还担保账户垫款
     */
    BATCH_REPAY_BAIL(
            "batchRepayBail",
            "/escrow/p2p/online"
    ),
    /**
     * 批次担保账户代偿
     */
    BATCH_BAIL_REPAY(
            "batchBailRepay",
            "/escrow/p2p/online"
    ),
    /**
     * 借款人受托支付申请
     */
    TRUSTEE_PAY(
            "trusteePay",
            "/escrow/p2p/page/trusteePay"),
    /**
     * 查询批次交易明细状态
     */
    BATCH_DETAILS_QUERY(
            "batchDetailsQuery",
            "/escrow/p2p/online"
    ),
    /**
     * 投资人投标申请查询
     */
    BID_APPLY_QUERY(
            "bidApplyQuery",
            "/escrow/p2p/online"),

    /**
     * 受托支付查询
     */
    TRUSTEE_PAY_QUERY(
            "trusteePayQuery",
            "/escrow/p2p/online"
    ),
    /**
     * 受托支付查询
     */
    ACCOUNT_QUERY_BY_MOBILE(
            "accountQueryByMobile",
            "/escrow/p2p/online"
    ),
    /**
     * 批次撤销
     */
    BATCH_CANCEL(
            "batchCancel",
            "/escrow/p2p/online"
    ),
    /**
     * 批次撤销
     */
    CREDIT_INVEST_QUERY(
            "creditInvestQuery",
            "/escrow/p2p/online"
    ),
    /**
     * 撤销投标申请
     */
    BID_CANCEL(
            "bidCancel",
            "/escrow/p2p/online"
    ),
    /**
     * 还款冻结解冻
     */
    BALANCE_UN_FREEZE(
            "balanceUnfreeze",
            "/escrow/p2p/online"
    ),
    /**
     * 还款冻结申请
     */
    BALANCE_FREEZE(
            "balanceFreeze",
            "/escrow/p2p/online"
    );

    private String value;
    private String url;

    private JixinTxCodeEnum(String value, String url) {
        this.value = value;
        this.url = url;
    }

    public String getValue() {
        return value;
    }

    public String getUrl() {
        return url;
    }
}
