package com.gofobao.framework.member.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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

    private Long incomeInterest;

    private Long incomeAward;

    private Long incomeOverdue;

    private Long incomeIntegralCash;

    private Long incomeBonus;

    private Long incomeOther;

    private Long waitCollectionPrincipal;

    private Long waitCollectionInterest;

    private Long tjWaitCollectionPrincipal;

    private Long tjWaitCollectionInterest;

    private Long qdWaitCollectionPrincipal;

    private Long qdWaitCollectionInterest;

    private Long expenditureInterest;

    private Long expenditureInterestManage;

    private Long expenditureManage;

    private Long expenditureFee;

    private Long expenditureOverdue;

    private Long expenditureOther;

    private Long waitRepayPrincipal;

    private Long waitRepayInterest;

    private Boolean tenderTuijian;

    private Boolean tenderJingzhi;

    private Boolean tenderMiao;

    private Boolean tenderTransfer;

    private Boolean tenderQudao;

    private Long firstTenderAward;

    private Long awardVirtualMoney;

    private Long rechargeTotal;

    private Date updatedAt;

    private Long yesterdayUseMoney;

    private Long cashTotal;


    public long getIncomeTotal() {
        return this.incomeInterest
                + this.incomeAward
                + this.incomeOverdue
                + this.incomeIntegralCash
                + this.getIncomeBonus()
                + this.incomeOther;
    }

    /**
     * 待付利息管理费
     *
     * @return
     */
    public int getWaitExpenditureInterestManageFee() {
        return (int) ((this.tjWaitCollectionInterest + this.qdWaitCollectionInterest) * 0.1);
    }

    /**
     * 当前用户是否是新手用户
     *
     * @return
     */
    public boolean isNovice() {
        return !(this.getTenderQudao() && this.getTenderTuijian());
    }

}
