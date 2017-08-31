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
            "/p2p/online",
            "移动端开户"),

    /**
     * 电子账户手机号修改增强
     */
    MOBILE_MODIFY_PLUS(
            "mobileModifyPlus",
            "/p2p/online",
            "电子账户手机号修改增强"),

    /**
     * 查询电子账户密码
     */
    PASSWORD_SET_QUERY(
            "passwordSetQuery",
            "/p2p/online",
            "查询电子账户密码"),

    /**
     * 开户
     */
    OPEN_ACCOUNT(
            "accountOpen",
            "/p2p/page/mobile",
            "后台开户"),

    /**
     * 查询用户资产
     */
    BALANCE_QUERY(
            "balanceQuery",
            "/p2p/online",
            "查询用户资产"),

    /**
     * 查询签约状态
     */
    CREDIT_AUTH_QUERY(
            "creditAuthQuery",
            "/p2p/online",
            "查询签约状态"),

    /**
     * 提现
     */
    WITH_DRAW(
            "withdraw",
            "/p2p/page/withdraw",
            "提现"),


    /**
     * 发送短信验证码
     */
    SMS_CODE_APPLY(
            "smsCodeApply",
            "/p2p/online",
            "短信发送"),


    BID_AUTO_APPLY(
            "bidAutoApply",
            "/p2p/online",
            "自动投标申请"
    ),

    /**
     * 初始化密码
     */
    PASSWORD_SET(
            "passwordSet",
            "/p2p/page/passwordset",
            "初始化密码"),

    /**
     * 密码重置
     */
    PASSWORD_RESET(
            "passwordReset",
            "/p2p/page/mobile",
            "密码重置"),

    /**
     * 红包发放
     */
    SEND_RED_PACKET(
            "voucherPay",
            "/p2p/online",
            "红包派发"),

    /**
     * 标的登记
     */
    DEBT_REGISTER(
            "debtRegister",
            "/p2p/online",
            "标的登记"),


    /**
     * 自动投标增强
     */
    AUTO_BID_AUTH_PLUS(
            "autoBidAuthPlus",
            "/p2p/page/mobile/plus",
            "自动投标增强"),

    /**
     * 自动投标
     */
    AUTO_BID_AUTH(
            "autoBidAuth",
            "/p2p/page/mobile",
            "自动投标签约"),
    /**
     * 债权转让增强
     */
    AUTO_CREDIT_INVEST_AUTH_PLUS(
            "autoCreditInvestAuthPlus",
            "/p2p/page/mobile/plus",
            "债权转让增强"),
    /**
     * 债权转让
     */
    AUTO_CREDIT_INVEST_AUTH(
            "autoCreditInvestAuth",
            "/p2p/page/mobile",
            "债权转让签约"),


    /**
     * 借款人标的撤销
     */
    DEBT_REGISTER_CANCEL(
            "debtRegisterCancel",
            "/p2p/online",
            "借款人标的撤销"),

    /**
     * 充值
     */
    DIRECT_RECHARGE_PLUS(
            "directRechargePlus",
            "/p2p/page/mobile/plus",
            "充值"),


    /**
     * 联机充值
     */
    DIRECT_RECHARGE_ONLINE(
            "directRechargeOnline",
            "/p2p/online",
            "联机充值"),

    /**
     * 借款人标的信息查询
     */
    DEBT_DETAILS_QUERY(
            "debtDetailsQuery",
            "/p2p/online",
            "借款人标的的信息查询"),
    /**
     * 批次投资人购买债权
     */
    BATCH_CREDIT_INVEST(
            "batchCreditInvest",
            "/p2p/online",
            "批次投资人购买债权"
    ),
    /**
     * 批次还款
     */
    BATCH_REPAY(
            "batchRepay",
            "/p2p/online",
            "批次还款"
    ),
    /**
     * 批次放款
     */
    BATCH_LEND_REPAY(
            "batchLendPay",
            "/p2p/online",
            "批次放款"
    ),
    /**
     * 电子账户资金交易明细查询
     */
    ACCOUNT_DETAILS_QUERY(
            "accountDetailsQuery",
            "/p2p/online",
            "电子账户资金交易明细查询"
    ),
    /**
     * 批次融资人还担保账户垫款
     */
    BATCH_REPAY_BAIL(
            "batchRepayBail",
            "/p2p/online",
            "批次融资人还担保账户垫款"
    ),
    /**
     * 批次名义借款人垫付
     */
    BATCH_BAIL_REPAY(
            "batchBailRepay",
            "/p2p/online",
            "批次名义借款人垫付"
    ),
    /**
     * 借款人受托支付申请
     */
    TRUSTEE_PAY(
            "trusteePay",
            "/p2p/page/trusteePay",
            "借款人受托支付申请"),
    /**
     * 查询批次交易明细状态
     */
    BATCH_DETAILS_QUERY(
            "batchDetailsQuery",
            "/p2p/online",
            "查询批次交易明细状态"
    ),
    /**
     * 投资人投标申请查询
     */
    BID_APPLY_QUERY(
            "bidApplyQuery",
            "/p2p/online",
            "投资人投标申请查询"),

    /**
     * 受托支付查询
     */
    TRUSTEE_PAY_QUERY(
            "trusteePayQuery",
            "/p2p/online",
            "受托支付查询"
    ),
    /**
     * 按证件号查询电子账号
     */
    ACCOUNT_ID_QUERY(
            "accountIdQuery",
            "/p2p/online",
            "按证件号查询电子账号"
    ),


    /**
     * 按手机号查询电子账号信息
     */
    ACCOUNT_QUERY_BY_MOBILE(
            "accountQueryByMobile",
            "/p2p/online",
            "按手机号查询电子账号信息"
    ),
    /**
     * 批次撤销
     */
    BATCH_CANCEL(
            "batchCancel",
            "/p2p/online",
            "批次撤销"
    ),
    /**
     * 批次结束债权
     */
    BATCH_CREDIT_END(
            "batchCreditEnd",
            "/p2p/online",
            "批次结束债权"
    ),
    /**
     * 投资人购买债权查询
     */
    CREDIT_INVEST_QUERY(
            "creditInvestQuery",
            "/p2p/online",
            "投资人购买债权查询"
    ),
    /**
     * 撤销投标申请
     */
    BID_CANCEL(
            "bidCancel",
            "/p2p/online",
            "撤销投标申请"
    ),
    /**
     * 还款冻结解冻
     */
    BALANCE_UN_FREEZE(
            "balanceUnfreeze",
            "/p2p/online",
            "还款冻结解冻"
    ),
    /**
     * 还款冻结申请
     */
    BALANCE_FREEZE(
            "balanceFreeze",
            "/p2p/online",
            "还款冻结申请"
    ),

    CREDIT_END(
            "creditEnd",
            "/p2p/online",
            "债权结束"
    ),
    BATCH_QUERY(
            "batchQuery",
            "/p2p/online",
            "查询批次状态"
    ),
    CREDIT_DETAILS_QUERY(
            "creditDetailsQuery",
            "/p2p/online",
            "债权明细查询"
    ),
    CARD_UNBIND(
            "cardUnbind",
            "/p2p/online",
            "解绑银行卡"
    ),
    CARD_BIND(
            "cardBind",
            "/p2p/page/mobile",
            "绑定银行卡"
    ),
    CARD_BIND_DETAILS_QUERY(
            "cardBindDetailsQuery",
                    "/p2p/online",
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
