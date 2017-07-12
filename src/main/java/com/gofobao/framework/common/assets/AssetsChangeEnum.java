package com.gofobao.framework.common.assets;

/**
 * 资金变动类型
 * Created by Max on 17/3/10.
 */
public enum AssetsChangeEnum {
    /**
     * 线上充值
     */
    OnlineRecharge(1, "7616"),

    /**
     * 线下充值
     */
    offlineRecharge(2, "7820"),

    /**
     * 小额提现提现
     */
    SmallCash(3, "2616", 100, "4616"),


    /**
     * 大额提现
     */
    BigCash(4, "2820", 101,"4820"),

    /**
     * 冻结资金
     */
    Frozen(5, ""),

    /**
     * 解除冻结
     */
    Unfrozen(6, ""),

    /**
     * 投标
     */
    Tender(7, "2780"),

    /**
     * 添加代收
     */
    CollectionAdd(8, ""),

    /**
     * 奖励
     */
    Award(9, "7833"),

    /**
     * 借款
     */
    Borrow(10, "7780"),

    /**
     * 添加待还
     */
    PaymentAdd(11, ""),

    /**
     * 正常还款
     */
    Repayment(12, "2781", 102, "2781"),

    /**
     * 扣除待还
     */
    PaymentLower(13, ""),

    /**
     * 回款
     */
    IncomeRepayment(14, "7781"),

    /**
     * 扣除待收
     */
    CollectionLower(15, "");

    private int type;
    private String  txType = "";
    private int feeType = 0 ;
    private String feeTxType = "" ;

    AssetsChangeEnum(int type, String txType) {
        this.type = type;
        this.txType = txType;
    }

    /** */
    AssetsChangeEnum(int type, String txType, int feeType, String feeTxType) {
        this.type = type;
        this.txType = txType;
        this.feeType = feeType;
        this.feeTxType = feeTxType;
    }

    public int getType() {
        return type;
    }

    public String getTxType() {
        return txType;
    }

    public int getFeeType() {
        return feeType;
    }

    public String getFeeTxType() {
        return feeTxType;
    }
}
