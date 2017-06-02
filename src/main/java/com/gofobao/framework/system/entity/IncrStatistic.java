package com.gofobao.framework.system.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Max on 17/6/2.
 */
@Entity
@Table(name = "gfb_incr_statistic")
@Data
@DynamicInsert
@NoArgsConstructor
@AllArgsConstructor
public class IncrStatistic {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Integer id;


    @Basic
    @Column(name = "register_count")
    private Integer registerCount;

    @Basic
    @Column(name = "register_total_count")
    private Integer registerTotalCount;

    @Basic
    @Column(name = "real_register_count")
    private Integer realRegisterCount;

    @Basic
    @Column(name = "real_register_total_count")
    private Integer realRegisterTotalCount;

    @Basic
    @Column(name = "tender_count")
    private Integer tenderCount;

    @Basic
    @Column(name = "tender_total")
    private Integer tenderTotal;

    @Basic
    @Column(name = "tender_tj_count")
    private Integer tenderTjCount;

    @Basic
    @Column(name = "tender_tj_total_count")
    private Integer tenderTjTotalCount;

    @Basic
    @Column(name = "tender_jz_count")
    private Integer tenderJzCount;

    @Basic
    @Column(name = "tender_jz_total_count")
    private Integer tenderJzTotalCount;

    @Basic
    @Column(name = "tender_miao_count")
    private Integer tenderMiaoCount;

    @Basic
    @Column(name = "tender_miao_total_count")
    private Integer tenderMiaoTotalCount;

    @Basic
    @Column(name = "tender_lz_count")
    private Integer tenderLzCount;

    @Basic
    @Column(name = "tender_lz_total_count")
    private Integer tenderLzTotalCount;

    @Basic
    @Column(name = "tender_qd_count")
    private Integer tenderQdCount;

    @Basic
    @Column(name = "tender_qd_total_count")
    private Integer tenderQdTotalCount;

    @Basic
    @Column(name = "recharge_sum")
    private Long rechargeSum;

    @Basic
    @Column(name = "cash_sum")
    private Long cashSum;

    @Basic
    @Column(name = "use_money_sum")
    private Long useMoneySum;

    @Basic
    @Column(name = "no_use_money_sum")
    private Integer noUseMoneySum;

    @Basic
    @Column(name = "tj_sum_publish")
    private Integer tjSumPublish;

    @Basic
    @Column(name = "jz_sum_publish")
    private Integer jzSumPublish;

    @Basic
    @Column(name = "lz_sum_publish")
    private Integer lzSumPublish;

    @Basic
    @Column(name = "qd_sum_publish")
    private Integer qdSumPublish;

    @Basic
    @Column(name = "miao_sum_publish")
    private Integer miaoSumPublish;

    @Basic
    @Column(name = "tj_sum_success")
    private Integer tjSumSuccess;

    @Basic
    @Column(name = "jz_sum_success")
    private Integer jzSumSuccess;

    @Basic
    @Column(name = "lz_sum_success")
    private Integer lzSumSuccess;

    @Basic
    @Column(name = "qd_sum_success")
    private Integer qdSumSuccess;

    @Basic
    @Column(name = "miao_sum_success")
    private Integer miaoSumSuccess;

    @Basic
    @Column(name = "tj_sum_repay")
    private Integer tjSumRepay;

    @Basic
    @Column(name = "tj_sum_repay_principal")
    private Integer tjSumRepayPrincipal;

    @Basic
    @Column(name = "jz_sum_repay")
    private Integer jzSumRepay;

    @Basic
    @Column(name = "qd_sum_repay")
    private Integer qdSumRepay;

    @Basic
    @Column(name = "qd_sum_repay_principal")
    private Integer qdSumRepayPrincipal;

    @Basic
    @Column(name = "jz_sum_repay_principal")
    private Integer jzSumRepayPrincipal;

    @Basic
    @Column(name = "tj_sum_repayment")
    private Long tjSumRepayment;

    @Basic
    @Column(name = "tj_sum_repayment_principal")
    private Long tjSumRepaymentPrincipal;

    @Basic
    @Column(name = "jz_sum_repayment")
    private Long jzSumRepayment;

    @Basic
    @Column(name = "qd_sum_repayment")
    private Long qdSumRepayment;

    @Basic
    @Column(name = "qd_sum_repayment_principal")
    private Long qdSumRepaymentPrincipal;

    @Basic
    @Column(name = "jz_sum_repayment_principal")
    private Long jzSumRepaymentPrincipal;

    @Basic
    @Column(name = "date")
    private Date date;
}
