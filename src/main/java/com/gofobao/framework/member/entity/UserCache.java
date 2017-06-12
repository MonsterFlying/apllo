package com.gofobao.framework.member.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/19.
 */
@Entity
@Table(name = "gfb_user_cache")
@Data
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
public class UserCache {
    @Id
    @Column(name = "user_id")
    // @GeneratedValue
    private Long userId;
    @Basic
    @Column(name = "income_interest")
    private Integer incomeInterest;
    @Basic
    @Column(name = "income_award")
    private Integer incomeAward;
    @Basic
    @Column(name = "income_overdue")
    private Integer incomeOverdue;
    @Basic
    @Column(name = "income_integral_cash")
    private Integer incomeIntegralCash;
    @Basic
    @Column(name = "income_bonus")
    private Integer incomeBonus;
    @Basic
    @Column(name = "income_other")
    private Integer incomeOther;
    @Basic
    @Column(name = "wait_collection_principal")
    private Integer waitCollectionPrincipal;
    @Basic
    @Column(name = "wait_collection_interest")
    private Integer waitCollectionInterest;
    @Basic
    @Column(name = "tj_wait_collection_principal")
    private Integer tjWaitCollectionPrincipal;
    @Basic
    @Column(name = "tj_wait_collection_interest")
    private Integer tjWaitCollectionInterest;
    @Basic
    @Column(name = "qd_wait_collection_principal")
    private Integer qdWaitCollectionPrincipal;
    @Basic
    @Column(name = "qd_wait_collection_interest")
    private Integer qdWaitCollectionInterest;
    @Basic
    @Column(name = "expenditure_interest")
    private Integer expenditureInterest;
    @Basic
    @Column(name = "expenditure_interest_manage")
    private Integer expenditureInterestManage;
    @Basic
    @Column(name = "expenditure_manage")
    private Integer expenditureManage;
    @Basic
    @Column(name = "expenditure_fee")
    private Integer expenditureFee;
    @Basic
    @Column(name = "expenditure_overdue")
    private Integer expenditureOverdue;
    @Basic
    @Column(name = "expenditure_other")
    private Integer expenditureOther;
    @Basic
    @Column(name = "wait_repay_principal")
    private Integer waitRepayPrincipal;
    @Basic
    @Column(name = "wait_repay_interest")
    private Integer waitRepayInterest;
    @Basic
    @Column(name = "tender_tuijian")
    private Boolean tenderTuijian;
    @Basic
    @Column(name = "tender_jingzhi")
    private Boolean tenderJingzhi;
    @Basic
    @Column(name = "tender_miao")
    private Boolean tenderMiao;
    @Basic
    @Column(name = "tender_transfer")
    private Boolean tenderTransfer;
    @Basic
    @Column(name = "tender_qudao")
    private Boolean tenderQudao;
    @Basic
    @Column(name = "first_tender_award")
    private Integer firstTenderAward;
    @Basic
    @Column(name = "award_virtual_money")
    private Integer awardVirtualMoney;
    @Basic
    @Column(name = "recharge_total")
    private Long rechargeTotal;
    @Basic
    @Column(name = "updated_at")
    private Date updatedAt;
    @Basic
    @Column(name = "yesterday_use_money")
    private Integer yesterdayUseMoney;
    @Basic
    @Column(name = "cash_total")
    private Long cashTotal;



    public int getIncomeTotal(){
        return this.incomeInterest
                + this.incomeAward
                + this.incomeOverdue
                + this.incomeIntegralCash
                + this.getIncomeBonus()
                + this.incomeOther ;
    }
    /**
     * 待付利息管理费
     *
     * @return
     */
    public int getWaitExpenditureInterestManageFee() {
        return (int) ((this.tjWaitCollectionInterest + this.qdWaitCollectionInterest) * 0.1);
    }
}
