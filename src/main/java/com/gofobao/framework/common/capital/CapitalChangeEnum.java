package com.gofobao.framework.common.capital;

/**
 * 资金变动类型
 * Created by Max on 17/3/10.
 */
public enum CapitalChangeEnum {
    /**
     * 充值
     */
    Recharge("recharge"),
    /**
     * 提现
     */
    Cash("cash"),

    /**
     * 冻结资金
     */
    Frozen("frozen"),
    /**
     * 提现资金
     */
    Unfrozen("unfrozen"),
    /**
     * 投标
     */
    Tender("tender"),
    /**
     * 添加代收
     */
    CollectionAdd("collection_add"),
    /**
     * 奖励
     */
    Award("award"),
    /**
     * 借款
     */
    Borrow("borrow"),
    /**
     * 借款
     */
    ACCRUED_INTEREST("accrued_interest"),
    /**
     * 添加待还
     */
    PaymentAdd("payment_add"),
    /**
     * 账户管理费
     */
    Manager("manager"),
    /**
     * 费用
     */
    Fee("fee"),
    /**
     * 还款
     */
    Repayment("repayment"),
    /**
     * new还款
     */
    NewRepayment("new_repayment"),
    /**
     * 扣除待还
     */
    PaymentLower("payment_lower"),
    /**
     * 逾期费
     */
    Overdue("overdue"),
    /**
     * new逾期费
     */
    NewOverdue("new_overdue"),
    /**
     * 回款
     */
    IncomeRepayment("income_repayment"),
    /**
     * 扣除待收
     */
    CollectionLower("collection_lower"),
    /**
     * 利息管理费
     */
    InterestManager("interest_manager"),
    /**
     * 收到逾期费
     */
    IncomeOverdue("income_overdue"),
    /**
     * 积分折现
     */
    IntegralCash("integral_cash"),
    /**
     * 提成
     */
    Bonus("bonus"),
    /**
     * 其他收入
     */
    IncomeOther("income_other"),

    /**
     * 其他收入
     */
    RedPackage("redPackage"),
    /**
     * 其他支出
     */
    ExpenditureOther("expenditure_other"),
    /**
     * 赠送体验金
     */
    AwardVirtualMoney("award_virtual_money"),
    /**
     * 投资体验标
     */
    VirtualTender("virtual_tender"),
    /**
     * 数据修正
     */
    Correct("correct");


    private String value;

    CapitalChangeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


}
