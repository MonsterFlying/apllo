package com.gofobao.framework.common.assets;


import com.gofobao.framework.common.capital.CapitalChangeEnum;

public enum AssetChangeTypeEnum {
    /**
     * 联机充值
     */
    onlineRecharge("在线充值", "online_recharge", "7616", "add@useMoney", "add@rechargeTotal", "D"),
    /**
     * 线下转账
     */
    offlineRecharge("线下转账", "offline_recharge", "7820", "add@useMoney", "add@rechargeTotal", "D"),

    /**
     * 资金冻结冻结
     */
    freeze("资金冻结", "freeze", "0", "sub@useMoney,add@noUseMoney", "", "B"),

    /**
     * 资金解冻
     */
    unfreeze("资金解冻", "unfreeze", "0", "add@useMoney,sub@noUseMoney", "", "B"),

    /**
     * 添加待收
     */
    collectionAdd("添加待收", "collectionAdd", "0", "add@collection", "add@waitCollectionPrincipal#principal,add@waitCollectionInterest#interest", "B"),
    /**
     * 添加待还
     */
    paymentAdd("添加待还", "paymentAdd", "0", "add@payment", "add@waitRepayPrincipal#principal,add@waitRepayInterest#interest", "B"),

    /**
     * 体验金投标
     */
    virtualTender("投资体验标", "virtualTender", "0", "sub@virtualMoney", "", "B"),

    /**
     * 减去待收
     */
    collectionSub("减去待收", "collectionSub", "0", "sub@collection", "sub@waitCollectionPrincipal#principal,sub@waitCollectionInterest#interest", "B"),
    /**
     * 减去待还
     */
    paymentSub("减去待还", "paymentSub", "0", "sub@payment", "sub@waitRepayPrincipal#principal,sub@waitRepayInterest#interest", "B"),

    /**
     * 借款人正常还款
     */
    repayment("还款", "repayment", "2781", "sub@noUseMoney", "add@expenditureInterest#interest", "C"),

    /**
     * 还款滞纳金
     */
    repayMentPenaltyFee("扣除还款滞纳金", "repayMentPenaltyFee", "4781", "sub@noUseMoney", "add@expenditureOverdue", "C"),

    /**
     * 平台收取还款滞纳金
     */
    platformRepayMentPenaltyFee("收取还款滞纳金", "platformRepayMentPenaltyFee", "7722", "add@useMoney", "add@expenditureOverdue", "D"),

    /**
     * 担保人代偿还款
     */
    compensatoryRepayment("代偿还款", "compensatoryRepayment", "2788", "sub@noUseMoney", "", "C"),


    /**
     * 代偿还款滞纳金
     */
    compensatoryRepaymentOverdueFee("代偿还款滞纳金", "compensatoryRepaymentOverdueFee", "2788", "sub@noUseMoney", "", "C"),

    /**
     * 出借人投标
     */
    tender("投标", "tender", "2780", "sub@noUseMoney", "", "C"),

    /**
     * 借款人借款入账
     */
    borrow("借款入账", "borrow", "7780", "add@useMoney", "", "D"),

    /**
     * 投资人到期收回本息
     */
    receivedPayments("正常回款", "receivedPayments", "7781", "add@useMoney", "add@incomeInterest#interest", "D"),

    /**
     * 投资人收到滞纳金
     */
    receivedPaymentsPenalty("还款滞纳金", "receivedPayments", "7781", "add@useMoney", "add@incomeOverdue", "D"),

    /**
     * 代偿账户收回代偿本息
     */
    compensatoryReceivedPayments("代偿账户代偿本息回款", "compensatoryReceivedPayments", "7788", "add@useMoney", "add@incomeInterest#interest", "D"),

    /**
     * 平台派发收益红包
     */
    publishIncomeRedpack("平台派发收益红包", "publishIncomeRedpack", "2792", "sub@useMoney", "", "C"),

    /**
     * 平台发放贴息红包
     */
    publishDiscountRedpack("平台发放贴息红包", "publishDiscountRedpack", "2793", "sub@useMoney", "", "C"),

    /**
     * 用户红包撤销
     */
    revokedRedpack("撤销红包", "revokedRedpack", "2833", "sub@useMoney", "", "C"),

    /**
     * 平台发放红包
     */
    publishRedpack("平台派发红包", "publishRedpack", "2833", "sub@useMoney", "", "C"),

    /**
     * 平台发放提成
     */
    publishCommissions("平台发放提成", "publishCommissions", "2833", "sub@useMoney", "", "C"),

    /**
     * 平台发放广富币兑换红包
     */
    platformPublishCurrencyExchangeRedpack("平台发放广富币兑换红包", "publishCurrencyExchangeRedpack", "2833", "sub@useMoney", "", "C"),

    /**
     * 平台发放积分兑换红包
     */
    platformPublishIntegralExchangeRedpack("平台发放积分兑换红包", "platformPublishIntegralExchangeRedpack", "2833", "sub@useMoney", "", "C"),

    /**
     * 用户接受平台广富币兑换红包
     */
    currencyExchangeRedpack("广富币兑换", "platformPublishIntegralExchangeRedpack", "7833", "add@useMoney", "add@incomeOther", "D"),

    /**
     * 红包奖励
     */
    receiveRedpack("红包奖励", "receiveRedpack", "7833", "add@useMoney", "add@incomeOther", "D"),

    /**
     * 接收提成
     */
    receiveCommissions("接收提成", "receiveCommissions", "7833", "add@useMoney", "add@incomeBonus", "D"),

    /**
     * 用户接受平台积分兑换红包
     */
    integralExchangeRedpack("积分兑换", "integralExchangeRedpack", "7833", "add@useMoney", "add@incomeOther", "D"),

    /**
     * 费用账户收取融资管理费
     */
    platformFinancingManagementFee("收取融资管理费", "platformFinancingManagementFee", "7722", "add@useMoney", "", "D"),

    /**
     * 融资管理费
     */
    financingManagementFee("融资管理费", "financingManagementFee", "9780", "sub@useMoney", "add@expenditureFee", "C"),

    /**
     * 费用账户收取利息管理费
     */
    platformInterestManagementFee("收取利息管理费", "platformInterestManagementFee", "7722", "add@useMoney", "", "D"),

    /**
     * 利息管理费
     */
    interestManagementFee("利息管理费", "interestManagementFee", "9781", "sub@useMoney", "add@expenditureInterestManage", "C" ),


    /**
     * 平台收取小额提现手续费
     */
    platformSmallCashFee("平台收取小额提现手续费", "platformSmallCashFee", "7722", "add@useMoney", "", "D"),

    /**
     * 平台收取大额提现手续费
     */
    platformBigCashFee("平台收取大额提现手续费", "platformBigCashFee", "7722", "add@useMoney", "", "D"),

    /**
     * 平台返还大额提现手续费
     */
    cancelPlatformBigCashFee("平台返还大额提现手续费", "cancelPlatformBigCashFee", "7722", "sub@useMoney", "", "D"),

    /**
     * 小额提现手续费
     */
    smallCashFee("小额提现手续费", "smallCashFee", "4616", "sub@useMoney", "add@expenditureFee", "C"),

    /**
     * 大额提现手续费
     */
    bigCashFee("大额提现手续费", "bigCashFee", "4820", "sub@useMoney", "add@expenditureFee", "C"),

    /**
     * 取消大额提现手续费
     */
    cancelBigCashFee("返还大额提现手续费", "cancelBigCashFee", "4820", "add@useMoney", "sub@expenditureFee", "D"),

    /**
     * 小额提现
     */
    smallCash("小额提现", "smallCash", "2616", "sub@useMoney", "add@cashTotal", "C"),

    /**
     * 大额提现
     */
    bigCash("大额提现", "bigCash", "2820", "sub@useMoney", "add@cashTotal", "C"),

    /**
     * 撤回大额提现
     */
    cancelBigCash("返还大额提现", "cancelBigCash", "2820", "add@useMoney", "sub@cashTotal", "D"),

    /**
     * 用户购买债权
     */
    batchBuyClaims("购买债权", "batchBuyClaims", "2789", "sub@noUseMoney", "", "C"),

    /**
     * 出售债权
     */
    batchSellBonds("出售债权", "batchSellBonds", "7785", "add@useMoney", "", "D"),

    /**
     * 出售债权手续费
     */
    batchSellBondsFee("出售债权手续费", "batchSellBondsFee", "9831", "sub@useMoney", "", "C"),

    /**
     * 平台收到债权转让手续费
     */
    platformBatchSellBondsFee("平台债权手续费入账", "platformBatchSellBondsFee", "7835", "add@useMoney", "", "D"),

    /**
     * 活期收益
     */
    currentIncome("活期收益", "currentIncome", "5500", "add@useMoney", "", "D");




    /**
     * 本地类型
     */
    private String localType;

    /**
     * 平台
     */
    private String platformType;
    /**
     * 资金变换标
     */
    private String assetChangeRule;

    /**
     * 资金变动标
     */
    private String userCacheChangeRule;

    /**
     * 操作名称
     */
    private String opName;

    private String txFlag ;

    public String getLocalType() {
        return localType;
    }

    public String getPlatformType() {
        return platformType;
    }

    public String getAssetChangeRule() {
        return assetChangeRule;
    }

    public String getUserCacheChangeRule() {
        return userCacheChangeRule;
    }

    public String getOpName() {
        return opName;
    }

    public String getTxFlag() {
        return txFlag;
    }

    AssetChangeTypeEnum(String opName,
                        String localType,
                        String platformType,
                        String assetChangeRule,
                        String userCacheChangeRule,
                        String txFlag) {
        this.opName = opName;
        this.localType = localType;
        this.platformType = platformType;
        this.assetChangeRule = assetChangeRule;
        this.userCacheChangeRule = userCacheChangeRule;
        this.txFlag = txFlag ;
    }

    public static AssetChangeTypeEnum findType(String localType) throws Exception{
        AssetChangeTypeEnum[] values = AssetChangeTypeEnum.values();
        for(AssetChangeTypeEnum typeEnum: values){
            if(typeEnum.getLocalType().equals(localType)){
                return typeEnum ;
            }
        }

        throw new Exception(String.format("没有该种类型的资金变动: %s", localType)) ;
    }
}
