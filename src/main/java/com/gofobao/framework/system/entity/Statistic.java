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
@Entity
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
public class Statistic {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id = 0L;

    @Basic
    @Column(name = "borrow_total")
    private Long borrowTotal = 0L;

    @Basic
    @Column(name = "tj_borrow_total")
    private Long tjBorrowTotal = 0L;

    @Basic
    @Column(name = "jz_borrow_total")
    private Long jzBorrowTotal = 0L;

    @Basic
    @Column(name = "mb_borrow_total")
    private Long mbBorrowTotal = 0L;

    @Basic
    @Column(name = "lz_borrow_total")
    private Long lzBorrowTotal = 0L;

    @Basic
    @Column(name = "qd_borrow_total")
    private Long qdBorrowTotal = 0L;

    @Basic
    @Column(name = "borrow_items")
    private Long borrowItems = 0L;

    @Basic
    @Column(name = "wait_repay_total")
    private Long waitRepayTotal = 0L;

    @Basic
    @Column(name = "tj_wait_repay_total")
    private Long tjWaitRepayTotal = 0L;

    @Basic
    @Column(name = "tj_wait_repay_principal_total")
    private Long tjWaitRepayPrincipalTotal = 0L;

    @Basic
    @Column(name = "qd_wait_repay_total")
    private Long qdWaitRepayTotal = 0L;

    @Basic
    @Column(name = "qd_wait_repay_principal_total")
    private Long qdWaitRepayPrincipalTotal = 0L;

    @Basic
    @Column(name = "jz_wait_repay_total")
    private Long jzWaitRepayTotal = 0L;

    @Basic
    @Column(name = "jz_wait_repay_principal_total")
    private Long jzWaitRepayPrincipalTotal = 0L;

    @Basic
    @Column(name = "user_income_total")
    private Long userIncomeTotal = 0L;

    @Basic
    @Column(name = "recharge_total")
    private Long rechargeTotal = 0L;

    @Basic
    @Column(name = "online_recharge_total")
    private Long onlineRechargeTotal = 0L;

    @Basic
    @Column(name = "offline_recharge_total")
    private Long offlineRechargeTotal = 0L;

    @Basic
    @Column(name = "cash_total")
    private Long cashTotal = 0L;

    @Basic
    @Column(name = "interest_total")
    private Long interestTotal =0L;

    @Basic
    @Column(name = "award_total")
    private Long awardTotal = 0L;

    @Basic
    @Column(name = "bonus_total")
    private Long bonusTotal = 0L;

    @Basic
    @Column(name = "integral_cash_total")
    private Long integralCashTotal = 0L;

    @Basic
    @Column(name = "other_zhichu_total")
    private Long otherZhichuTotal = 0L;

    @Basic
    @Column(name = "manager_fee_total")
    private Long managerFeeTotal  = 0L;

    @Basic
    @Column(name = "interest_manager_total")
    private Long interestManagerTotal  = 0L ;

    @Basic
    @Column(name = "other_shouru_total")
    private Long otherShouruTotal  = 0L;

    @Basic
    @Column(name = "updated_at")
    private Date updatedAt;

}
