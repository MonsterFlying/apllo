package com.gofobao.framework.asset.contants;

/**
 * 资产类型
 * Created by Max on 17/3/6.
 */
public class AssetTypeContants {
    /**
     * 充值
     */
    public static final String RECHARGE = "recharge";
    /**
     * 提现
     */
    public static final String CASH = "cash";
    /**
     * 冻结资金
     */
    public static final String FROZEN = "frozen";
    /**
     * 解除冻结
     */
    public static final String UNFROZEN = "unfrozen";
    /**
     * 投标
     */
    public static final String TENDER = "tender";
    /**
     * 添加待收
     */
    public static final String COLLECTION_ADD = "collection_add";
    /**
     * 奖励
     */
    public static final String AWARD = "award";
    /**
     * 借款
     */
    public static final String BORROW = "borrow";
    /**
     * 添加待还
     */
    public static final String PAYMENT_ADD = "payment_add";
    /**
     * 账户管理费
     */
    public static final String MANAGER = "manager";
    /**
     * 费用
     */
    public static final String FEE = "fee";
    /**
     * 还款
     */
    public static final String REPAYMENT = "repayment";
    /**
     * 扣除待还
     */
    public static final String PAYMENT_LOWER = "payment_lower";
    /**
     * 逾期费
     */
    public static final String OVERDUE = "overdue";
    /**
     * 回款
     */
    public static final String INCOME_REPAYMENT = "income_repayment";
    /**
     * 扣除待收
     */
    public static final String COLLECTION_LOWER = "collection_lower";
    /**
     * 利息管理费
     */
    public static final String INTEREST_MANAGER = "interest_manager";
    /**
     * 积分折现
     */
    public static final String INTEGRAL_CASH = "integral_cash";
    /**
     * 提成
     */
    public static final String BONUS = "bonus";
    /**
     * 其他收入
     */
    public static final String INCOME_OTHER = "income_other";

    /**
     * 红包
     */
    public static final String  REDPACKAGE ="red_package";
    /**
     * 其他支出
     */
    public static final String EXPENDITURE_OTHER = "expenditure_other";
    /**
     * 奖励体验金
     */
    public static final String AWARD_VIRTUAL_MONEY = "award_virtual_money";
    /**
     * 投资体验标
     */
    public static final String VIRTUAL_TENDER = "virtual_tender";
    /**
     * 数据修正
     */
    public static final String CORRECT = "correct";
    /**
     * 逾期收入
     */
    public static final String INCOME_OVERDUE = "income_overdue";

    /**
     * 获取交易记录接口需要展示的类型
     */
    public static final String[] getAssetLogType = new String[]{RECHARGE, AWARD, BORROW, INCOME_REPAYMENT, INCOME_OVERDUE, INTEGRAL_CASH, BONUS,
             INCOME_OTHER, CASH, TENDER, MANAGER, FEE, REPAYMENT, OVERDUE, INTEREST_MANAGER, EXPENDITURE_OTHER,
             FROZEN, UNFROZEN, CORRECT ,REDPACKAGE};
}
