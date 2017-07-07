package com.gofobao.framework.common.assets;

/**
 *  资金变动类型
 *  Created by Administrator on 2017/7/7 0007.
 */
public enum AssetChangeTypeEnum {
    /**
     * 提现支出
     */
    expenditureCash(2, "2616", 12, "4616"),

    /**
     * 投标支出
     */
    expenditureTender(3, "2780", 13, "4780"),

    /**
     * 到期还款支出
     */
    expenditureRepayment(4, "2781", 14, "4781"),

    /**
     * 代偿还款支出
     */
    expenditureReplacementRepayment(5, "2788", 15, "4788"),

    /**
     * 批量债权转让-购买债权 支出
     */
    expenditureBatchTransferOut(6, "2789"),

    /**
     * P2P红包发放收益扣款 支出
     */
    expenditureBatchRedPackIncomeOut(7,"2792"),

    /**
     * P2P平台贴息收益扣款 支出
     */
    expenditureBatchRedPackInterestOut(8, "interest"),

    /**
     * 大额提现（行内渠道资金转出、二代支付转出） 支出
     */
    expenditureBigCash(9, "2820"),

    /**
     * 债权转让资金转出 支出
     */
    expenditureTransferOut(10, "2831"),

    /**
     * 红包派发 支出
     */
    expenditureRedpackPublish(11, "2833"),

    /**
     * 撤销红包派发 支出
     */
    expenditureRedpackCancel(11, "2833"),

    /**
     * 提现手续费 支出
     */
    expenditureCashFee(12, "4616"),

    /**
     * 投标手续费 支出
     */
    expenditureTenderFee(13, "4780"),

    /**
     * 到期还款手续费 支出
     */
    expenditureRepaymentFee(14, "4781"),

    /**
     * 代偿还款手续费 支出
     */
    expenditureReplacementRepaymentFee(15, "4788"),

    /**
     * 中间业务转出手续费 支出
     */
    expenditureTxFee(16, "4820"),


    /**
     * 获取收益
     */
    incomeCurrent(17, "5500"),

    /**
     * 靠档计息
     */
    incomeCOffsetInterest(18, "5504"),

    /**
     * 联机充值
     */
    incomeOnLineRecharge(19, "7616"),

    /**
     * 手续费的入账
     */
    incomefee(20, "7722");
















    /**
     * 平台资金变动类型
     */
    private int type ;

    /**
     * 即信资金 变动类型
     */
    private String jixinTxType ;

    /**
     * 费用类型
     */
    private int feeType ;

    /**
     * 即信资金 费用变动类型
     */
    private String jixinTxType4Fee ;

    AssetChangeTypeEnum(int type, String jixinTxType) {
        this.type = type;
        this.jixinTxType = jixinTxType;
    }

    AssetChangeTypeEnum(int type, String jixinTxType, int feeType, String jixinTxType4Fee) {
        this.type = type;
        this.jixinTxType = jixinTxType;
        this.feeType = feeType;
        this.jixinTxType4Fee = jixinTxType4Fee;
    }

    public int getType() {
        return type;
    }

    public String getJixinTxType() {
        return jixinTxType;
    }
}
