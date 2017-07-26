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
            "/escrow/p2p/online",
            "移动端开户"),


    /**
     * 开户
     */
    OPEN_ACCOUNT(
            "accountOpen",
            "/escrow/p2p/page/mobile",
            "后台开户"),

    /**
     * 查询用户资产
     */
    BALANCE_QUERY(
            "balanceQuery",
            "/escrow/p2p/online",
            "查询用户资产"),

    /**
     * 查询签约状态
     */
    CREDIT_AUTH_QUERY(
            "creditAuthQuery",
            "/escrow/p2p/online",
            "查询签约状态"),

    /**
     * 提现
     */
    WITH_DRAW(
            "withdraw",
            "/escrow/p2p/page/withdraw",
            "提现"),


    /**
     * 发送短信验证码
     */
    SMS_CODE_APPLY(
            "smsCodeApply",
            "/escrow/p2p/online",
            "短信发送"),


    BID_AUTO_APPLY(
            "bidAutoApply",
            "/escrow/p2p/online",
            "自动投标申请"
    ),

    /**
     * 初始化密码
     */
    PASSWORD_SET(
            "passwordSet",
            "/escrow/p2p/page/passwordset",
            "初始化密码"),

    /**
     * 密码重置
     */
    PASSWORD_RESET(
            "passwordReset",
            "/escrow/p2p/page/mobile",
            "密码重置"),

    /**
     * 红包发放
     */
    SEND_RED_PACKET(
            "voucherPay",
            "/escrow/p2p/online",
            "红包派发"),

    /**
     * 标的登记
     */
    DEBT_REGISTER(
            "debtRegister",
            "/escrow/p2p/online",
            "标的登记"),


    /**
     * 自动投标增强
     */
    AUTO_BID_AUTH_PLUS(
            "autoBidAuthPlus",
            "/escrow/p2p/page/mobile/plus",
            "自动投标增强"),

    /**
     * 自动投标
     */
    AUTO_BID_AUTH(
            "autoBidAuth",
            "/escrow/p2p/page/mobile",
            "自动投标签约"),
    /**
     * 债权转让增强
     */
    AUTO_CREDIT_INVEST_AUTH_PLUS(
            "autoCreditInvestAuthPlus",
            "/escrow/p2p/page/mobile/plus",
            "债权转让增强"),
    /**
     * 债权转让
     */
    AUTO_CREDIT_INVEST_AUTH(
            "autoCreditInvestAuth",
            "/escrow/p2p/page/mobile",
            "债权转让签约"),


    /**
     * 借款人标的撤销
     */
    DEBT_REGISTER_CANCEL(
            "debtRegisterCancel",
            "/escrow/p2p/online",
            "借款人标的撤销"),

    /**
     * 充值
     */
    DIRECT_RECHARGE_PLUS(
            "directRechargePlus",
            "/escrow/p2p/page/mobile/plus",
            "充值"),


    /**
     * 联机充值
     */
    DIRECT_RECHARGE_ONLINE(
            "directRechargeOnline",
            "/escrow/p2p/online",
            "联机充值"),

    /**
     * 借款人标的信息查询
     */
    DEBT_DETAILS_QUERY(
            "debtDetailsQuery",
            "/escrow/p2p/online",
            "借款人标的的信息查询"),
    /**
     * 批次投资人购买债权
     */
    BATCH_CREDIT_INVEST(
            "batchCreditInvest",
            "/escrow/p2p/online",
            "批次投资人购买债权"
    ),
    /**
     * 批次还款
     */
    BATCH_REPAY(
            "batchRepay",
            "/escrow/p2p/online",
            "批次还款"
    ),
    /**
     * 批次放款
     */
    BATCH_LEND_REPAY(
            "batchLendPay",
            "/escrow/p2p/online",
            "批次放款"
    ),
    /**
     * 电子账户资金交易明细查询
     */
    ACCOUNT_DETAILS_QUERY(
            "accountDetailsQuery",
            "/escrow/p2p/online",
            "电子账户资金交易明细查询"
    ),
    /**
     * 批次融资人还担保账户垫款
     */
    BATCH_REPAY_BAIL(
            "batchRepayBail",
            "/escrow/p2p/online",
            "批次融资人还担保账户垫款"
    ),
    /**
     * 批次担保账户代偿
     */
    BATCH_BAIL_REPAY(
            "batchBailRepay",
            "/escrow/p2p/online",
            "批次担保账户代偿"
    ),
    /**
     * 借款人受托支付申请
     */
    TRUSTEE_PAY(
            "trusteePay",
            "/escrow/p2p/page/trusteePay",
            "借款人受托支付申请"),
    /**
     * 查询批次交易明细状态
     */
    BATCH_DETAILS_QUERY(
            "batchDetailsQuery",
            "/escrow/p2p/online",
            "查询批次交易明细状态"
    ),
    /**
     * 投资人投标申请查询
     */
    BID_APPLY_QUERY(
            "bidApplyQuery",
            "/escrow/p2p/online",
            "投资人投标申请查询"),

    /**
     * 受托支付查询
     */
    TRUSTEE_PAY_QUERY(
            "trusteePayQuery",
            "/escrow/p2p/online",
            "受托支付查询"
    ),
    /**
     * 按手机号查询电子账号信息
     */
    ACCOUNT_QUERY_BY_MOBILE(
            "accountQueryByMobile",
            "/escrow/p2p/online",
            "按手机号查询电子账号信息"
    ),
    /**
     * 批次撤销
     */
    BATCH_CANCEL(
            "batchCancel",
            "/escrow/p2p/online",
            "批次撤销"
    ),
    /**
     * 批次结束债权
     */
    BATCH_CREDIT_END(
            "batchCreditEnd",
            "/escrow/p2p/online",
            "批次结束债权"
    ),
    /**
     * 投资人购买债权查询
     */
    CREDIT_INVEST_QUERY(
            "creditInvestQuery",
            "/escrow/p2p/online",
            "投资人购买债权查询"
    ),
    /**
     * 撤销投标申请
     */
    BID_CANCEL(
            "bidCancel",
            "/escrow/p2p/online",
            "撤销投标申请"
    ),
    /**
     * 还款冻结解冻
     */
    BALANCE_UN_FREEZE(
            "balanceUnfreeze",
            "/escrow/p2p/online",
            "还款冻结解冻"
    ),
    /**
     * 还款冻结申请
     */
    BALANCE_FREEZE(
            "balanceFreeze",
            "/escrow/p2p/online",
            "还款冻结申请"
    ),

    CREDIT_END(
            "creditEnd",
            "/escrow/p2p/online",
            "债权结束"
    ),
    BATCH_QUERY(
            "batchQuery",
            "/escrow/p2p/online",
            "查询批次状态"
    ),
    CREDIT_DETAILS_QUERY(
            "creditDetailsQuery",
            "/escrow/p2p/online",
            "债权明细查询"
    ),
    CARD_UNBIND(
            "cardUnbind",
            "/escrow/p2p/online",
            "解绑银行卡"
    ),
    CARD_BIND(
            "cardBind",
            "/escrow/p2p/page/mobile",
            "绑定银行卡"
    ),
    CARD_BIND_DETAILS_QUERY(
            "cardBindDetailsQuery",
                    "/escrow/p2p/online",
                    "绑卡关系查询"
    );


    private String value;
    private String url;
    private String name ;

    JixinTxCodeEnum(String value, String url, String name) {
        this.value = value;
        this.url = url;
        this.name = name ;
    }

    public String getValue() {
        return value;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }
}
