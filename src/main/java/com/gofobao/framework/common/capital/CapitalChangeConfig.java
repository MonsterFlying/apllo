package com.gofobao.framework.common.capital;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Max on 17/3/10.
 */
public class CapitalChangeConfig {
    /**
     * 资金变动类型
     */
    private CapitalChangeEnum type;

    /**
     * 资金变动名称
     */
    private String name;

    /**
     * 资金变动规则
     */
    private String assetChangeRule;

    /**
     * 用户缓存资金变动规则
     */
    private String userCacheChangeRule;

    /**
     * 备注
     */
    private String remark;

    /**
     * 规则类
     */
    public final static List<CapitalChangeConfig> capitalChangeList = new ArrayList<>();

    static {
        //充值
        CapitalChangeConfig recharge = new CapitalChangeConfig();
        recharge.setType(CapitalChangeEnum.Recharge);
        recharge.setName("充值");
        recharge.setAssetChangeRule("add@useMoney");
        recharge.setUserCacheChangeRule("add@rechargeTotal");
        recharge.setRemark("充值成功");
        capitalChangeList.add(recharge);
        //提现
        CapitalChangeConfig cash = new CapitalChangeConfig();
        cash.setType(CapitalChangeEnum.Cash);
        cash.setName("提现");
        cash.setAssetChangeRule("sub@useMoney");  // 直接扣除可用
        cash.setUserCacheChangeRule("add@cashTotal");
        cash.setRemark("提现成功");
        capitalChangeList.add(cash);
        // 冻结
        CapitalChangeConfig frozen = new CapitalChangeConfig();
        frozen.setType(CapitalChangeEnum.Frozen);
        frozen.setName("冻结资金");
        frozen.setAssetChangeRule("sub@useMoney,add@noUseMoney");
        frozen.setUserCacheChangeRule(null);
        frozen.setRemark("冻结资金");
        capitalChangeList.add(frozen);
        // 解冻
        CapitalChangeConfig unfrozen = new CapitalChangeConfig();
        unfrozen.setType(CapitalChangeEnum.Unfrozen);
        unfrozen.setName("解除资金冻结");
        unfrozen.setAssetChangeRule("add@useMoney,sub@noUseMoney");
        unfrozen.setUserCacheChangeRule(null);
        unfrozen.setRemark("解除资金冻结");
        capitalChangeList.add(unfrozen);
        // 投标
        CapitalChangeConfig tender = new CapitalChangeConfig();
        tender.setType(CapitalChangeEnum.Tender);
        tender.setName("投标");
        tender.setAssetChangeRule("sub@noUseMoney");
        tender.setUserCacheChangeRule(null);
        tender.setRemark("投标成功");
        capitalChangeList.add(tender);
        // 添加待收
        CapitalChangeConfig collectionAdd = new CapitalChangeConfig();
        collectionAdd.setType(CapitalChangeEnum.CollectionAdd);
        collectionAdd.setName("添加待收");
        collectionAdd.setAssetChangeRule("add@collection");
        collectionAdd.setUserCacheChangeRule("add@waitCollectionPrincipal#principal,add@waitCollectionInterest#interest");
        collectionAdd.setRemark("添加待收");
        capitalChangeList.add(collectionAdd);
        // 奖励
        CapitalChangeConfig award = new CapitalChangeConfig();
        award.setType(CapitalChangeEnum.Award);
        award.setName("奖励");
        award.setAssetChangeRule("add@useMoney");
        award.setUserCacheChangeRule("add@incomeAward");
        award.setRemark("奖励");
        capitalChangeList.add(award);
        // 借款
        CapitalChangeConfig borrow = new CapitalChangeConfig();
        borrow.setType(CapitalChangeEnum.Borrow);
        borrow.setName("借款");
        borrow.setAssetChangeRule("add@useMoney");
        borrow.setUserCacheChangeRule(null);
        borrow.setRemark("借款成功");
        capitalChangeList.add(borrow);
        // 添加待还
        CapitalChangeConfig paymentAdd = new CapitalChangeConfig();
        paymentAdd.setType(CapitalChangeEnum.PaymentAdd);
        paymentAdd.setName("添加待还");
        paymentAdd.setAssetChangeRule("add@payment");
        paymentAdd.setUserCacheChangeRule("add@waitRepayPrincipal#principal,add@waitRepayInterest#interest");
        paymentAdd.setRemark("添加待还");
        capitalChangeList.add(paymentAdd);
        // 账户管理费
        CapitalChangeConfig manager = new CapitalChangeConfig();
        manager.setType(CapitalChangeEnum.Manager);
        manager.setName("账户管理费");
        manager.setAssetChangeRule("sub@useMoney");
        manager.setUserCacheChangeRule("add@expenditureManage");
        manager.setRemark("账户管理费");
        capitalChangeList.add(manager);
        // 费用
        CapitalChangeConfig fee = new CapitalChangeConfig();
        fee.setType(CapitalChangeEnum.Fee);
        fee.setName("费用");
        fee.setAssetChangeRule("sub@useMoney");
        fee.setUserCacheChangeRule("add@expenditureFee");
        fee.setRemark("费用");
        capitalChangeList.add(fee);
        //还款
        CapitalChangeConfig repayment = new CapitalChangeConfig();
        repayment.setType(CapitalChangeEnum.Repayment);
        repayment.setName("还款");
        repayment.setAssetChangeRule("sub@useMoney");
        repayment.setUserCacheChangeRule("add@expenditureInterest#interest");
        repayment.setRemark("还款");
        capitalChangeList.add(repayment);
        //扣除待还
        CapitalChangeConfig paymentLower = new CapitalChangeConfig();
        paymentLower.setType(CapitalChangeEnum.PaymentLower);
        paymentLower.setName("扣除待还");
        paymentLower.setAssetChangeRule("sub@payment");
        paymentLower.setUserCacheChangeRule("sub@waitRepayPrincipal#principal,sub@waitRepayInterest#interest");
        paymentLower.setRemark("扣除待还");
        capitalChangeList.add(paymentLower);
        //逾期费
        CapitalChangeConfig overdue = new CapitalChangeConfig();
        overdue.setType(CapitalChangeEnum.Overdue);
        overdue.setName("逾期费");
        overdue.setAssetChangeRule("sub@useMoney");
        overdue.setUserCacheChangeRule("add@expenditureOverdue");
        overdue.setRemark("逾期费");
        capitalChangeList.add(overdue);
        //回款
        CapitalChangeConfig incomeRepayment = new CapitalChangeConfig();
        incomeRepayment.setType(CapitalChangeEnum.IncomeRepayment);
        incomeRepayment.setName("回款");
        incomeRepayment.setAssetChangeRule("add@useMoney");
        incomeRepayment.setUserCacheChangeRule("add@incomeInterest#interest");
        incomeRepayment.setRemark("回款");
        capitalChangeList.add(incomeRepayment);
        //扣除待收
        CapitalChangeConfig collectionLower = new CapitalChangeConfig();
        collectionLower.setType(CapitalChangeEnum.CollectionLower);
        collectionLower.setName("扣除待收");
        collectionLower.setAssetChangeRule("sub@collection");
        collectionLower.setUserCacheChangeRule("sub@waitCollectionPrincipal#principal,sub@waitCollectionInterest#interest");
        collectionLower.setRemark("扣除待收");
        capitalChangeList.add(collectionLower);
        //利息管理费
        CapitalChangeConfig interestManager = new CapitalChangeConfig();
        interestManager.setType(CapitalChangeEnum.InterestManager);
        interestManager.setName("利息管理费");
        interestManager.setAssetChangeRule("sub@useMoney");
        interestManager.setUserCacheChangeRule("add@expenditureInterestManage");
        interestManager.setRemark("利息管理费");
        capitalChangeList.add(interestManager);
        //收到逾期费
        CapitalChangeConfig incomeOverdue = new CapitalChangeConfig();
        incomeOverdue.setType(CapitalChangeEnum.IncomeOverdue);
        incomeOverdue.setName("收到逾期费");
        incomeOverdue.setAssetChangeRule("add@useMoney");
        incomeOverdue.setUserCacheChangeRule("add@incomeOverdue");
        incomeOverdue.setRemark("收到逾期费");
        capitalChangeList.add(incomeOverdue);
        //积分折现
        CapitalChangeConfig integralCash = new CapitalChangeConfig();
        integralCash.setType(CapitalChangeEnum.IntegralCash);
        integralCash.setName("积分折现");
        integralCash.setAssetChangeRule("add@useMoney");
        integralCash.setUserCacheChangeRule("add@incomeIntegralCash");
        integralCash.setRemark("积分折现");
        capitalChangeList.add(integralCash);
        //提成
        CapitalChangeConfig bonus = new CapitalChangeConfig();
        bonus.setType(CapitalChangeEnum.Bonus);
        bonus.setName("提成");
        bonus.setAssetChangeRule("add@useMoney");
        bonus.setUserCacheChangeRule("add@incomeBonus");
        bonus.setRemark("提成");
        capitalChangeList.add(bonus);
        //其他支出
        CapitalChangeConfig expenditureOther = new CapitalChangeConfig();
        expenditureOther.setType(CapitalChangeEnum.ExpenditureOther);
        expenditureOther.setName("其他支出");
        expenditureOther.setAssetChangeRule("sub@useMoney");
        expenditureOther.setUserCacheChangeRule("add@expenditureOther");
        expenditureOther.setRemark("其他支出");
        capitalChangeList.add(expenditureOther);
        //其它收入
        CapitalChangeConfig incomeOther = new CapitalChangeConfig();
        incomeOther.setType(CapitalChangeEnum.IncomeOther);
        incomeOther.setName("其他收入");
        incomeOther.setAssetChangeRule("add@useMoney");
        incomeOther.setUserCacheChangeRule("add@incomeOther");
        incomeOther.setRemark("其他收入");
        capitalChangeList.add(incomeOther);
        //红包收入
        CapitalChangeConfig redPackage = new CapitalChangeConfig();
        redPackage.setType(CapitalChangeEnum.RedPackage);
        redPackage.setName("红包收入");
        redPackage.setAssetChangeRule("add@useMoney");
        redPackage.setUserCacheChangeRule("add@incomeOther");
        redPackage.setRemark("红包收入");
        capitalChangeList.add(redPackage);

        //奖励体验金
        CapitalChangeConfig awardVirtualMoney = new CapitalChangeConfig();
        awardVirtualMoney.setType(CapitalChangeEnum.AwardVirtualMoney);
        awardVirtualMoney.setName("奖励体验金");
        awardVirtualMoney.setAssetChangeRule("add@virtualMoney");
        awardVirtualMoney.setUserCacheChangeRule(null);
        awardVirtualMoney.setRemark("奖励体验金");
        capitalChangeList.add(awardVirtualMoney);
        //投资体验标
        CapitalChangeConfig virtualTender = new CapitalChangeConfig();
        virtualTender.setType(CapitalChangeEnum.VirtualTender);
        virtualTender.setName("投资体验标");
        virtualTender.setAssetChangeRule("sub@virtualMoney");
        virtualTender.setUserCacheChangeRule(null);
        virtualTender.setRemark("投资体验标");
        capitalChangeList.add(virtualTender);
        //数据修正
        CapitalChangeConfig correct = new CapitalChangeConfig();
        correct.setType(CapitalChangeEnum.Correct);
        correct.setName("数据修正");
        correct.setAssetChangeRule(null);
        correct.setUserCacheChangeRule(null);
        correct.setRemark("数据调整");
        capitalChangeList.add(correct);
    }

    public CapitalChangeEnum getType() {
        return type;
    }

    public void setType(CapitalChangeEnum type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAssetChangeRule() {
        return assetChangeRule;
    }

    public void setAssetChangeRule(String assetChangeRule) {
        this.assetChangeRule = assetChangeRule;
    }

    public String getUserCacheChangeRule() {
        return userCacheChangeRule;
    }

    public void setUserCacheChangeRule(String userCacheChangeRule) {
        this.userCacheChangeRule = userCacheChangeRule;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
