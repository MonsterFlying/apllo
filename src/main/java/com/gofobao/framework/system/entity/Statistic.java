package com.gofobao.framework.system.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * 全站统计
 * Created by Max on 17/6/2.
 */
@Table(name = "gfb_statistic")
@Data
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
public class Statistic {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;

    @Basic
    @Column(name = "borrow_total")
    private Long borrowTotal;

    @Basic
    @Column(name = "tj_borrow_total")
    private Long tjBorrowTotal;

    @Basic
    @Column(name = "jz_borrow_total")
    private Long jzBorrowTotal;

    @Basic
    @Column(name = "mb_borrow_total")
    private Long mbBorrowTotal;

    @Basic
    @Column(name = "lz_borrow_total")
    private Long lzBorrowTotal;

    @Basic
    @Column(name = "qd_borrow_total")
    private Long qdBorrowTotal;

    @Basic
    @Column(name = "borrow_items")
    private Long borrowItems;

    @Basic
    @Column(name = "wait_repay_total")
    private Long waitRepayTotal;

    @Basic
    @Column(name = "tj_wait_repay_total")
    private Long tjWaitRepayTotal;

    @Basic
    @Column(name = "tj_wait_repay_principal_total")
    private Long tjWaitRepayPrincipalTotal;

    @Basic
    @Column(name = "qd_wait_repay_total")
    private Long qdWaitRepayTotal;

    @Basic
    @Column(name = "qd_wait_repay_principal_total")
    private Long qdWaitRepayPrincipalTotal;

    @Basic
    @Column(name = "jz_wait_repay_total")
    private Long jzWaitRepayTotal;

    @Basic
    @Column(name = "jz_wait_repay_principal_total")
    private Long jzWaitRepayPrincipalTotal;

    @Basic
    @Column(name = "user_income_total")
    private Long userIncomeTotal;

    @Basic
    @Column(name = "recharge_total")
    private Long rechargeTotal;

    @Basic
    @Column(name = "online_recharge_total")
    private Long onlineRechargeTotal;

    @Basic
    @Column(name = "offline_recharge_total")
    private Long offlineRechargeTotal;

    @Basic
    @Column(name = "cash_total")
    private Long cashTotal;

    @Basic
    @Column(name = "interest_total")
    private Long interestTotal;

    @Basic
    @Column(name = "award_total")
    private Long awardTotal;

    @Basic
    @Column(name = "bonus_total")
    private Long bonusTotal;

    @Basic
    @Column(name = "integral_cash_total")
    private Long integralCashTotal;

    @Basic
    @Column(name = "other_zhichu_total")
    private Long otherZhichuTotal;

    @Basic
    @Column(name = "manager_fee_total")
    private Long managerFeeTotal;

    @Basic
    @Column(name = "interest_manager_total")
    private Long interestManagerTotal;

    @Basic
    @Column(name = "other_shouru_total")
    private Long otherShouruTotal;

    @Basic
    @Column(name = "updated_at")
    private Date updatedAt;

}
