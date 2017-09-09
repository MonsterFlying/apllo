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

@Table(name = "gfb_incr_statistic")
@Data
@Entity
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
    private Integer registerCount = 0;

    @Basic
    @Column(name = "register_total_count")
    private Integer registerTotalCount = 0;

    @Basic
    @Column(name = "real_register_count")
    private Integer realRegisterCount = 0;

    @Basic
    @Column(name = "real_register_total_count")
    private Integer realRegisterTotalCount = 0;

    @Basic
    @Column(name = "tender_count")
    private Integer tenderCount = 0;

    @Basic
    @Column(name = "tender_total")
    private Integer tenderTotal = 0;

    @Basic
    @Column(name = "tender_tj_count")
    private Integer tenderTjCount = 0;

    @Basic
    @Column(name = "tender_tj_total_count")
    private Integer tenderTjTotalCount = 0;

    @Basic
    @Column(name = "tender_jz_count")
    private Integer tenderJzCount = 0;

    @Basic
    @Column(name = "tender_jz_total_count")
    private Integer tenderJzTotalCount = 0;

    @Basic
    @Column(name = "tender_miao_count")
    private Integer tenderMiaoCount = 0;

    @Basic
    @Column(name = "tender_miao_total_count")
    private Integer tenderMiaoTotalCount = 0;

    @Basic
    @Column(name = "tender_lz_count")
    private Integer tenderLzCount = 0;

    @Basic
    @Column(name = "tender_lz_total_count")
    private Integer tenderLzTotalCount = 0;

    @Basic
    @Column(name = "tender_qd_count")
    private Integer tenderQdCount = 0;

    @Basic
    @Column(name = "tender_qd_total_count")
    private Integer tenderQdTotalCount = 0;

    @Basic
    @Column(name = "recharge_sum")
    private Long rechargeSum = 0l;

    @Basic
    @Column(name = "cash_sum")
    private Long cashSum = 0l;

    @Basic
    @Column(name = "use_money_sum")
    private Long useMoneySum = 0l;

    @Basic
    @Column(name = "no_use_money_sum")
    private Integer noUseMoneySum = 0;

    @Basic
    @Column(name = "tj_sum_publish")
    private Integer tjSumPublish = 0;

    @Basic
    @Column(name = "jz_sum_publish")
    private Integer jzSumPublish = 0;

    @Basic
    @Column(name = "lz_sum_publish")
    private Integer lzSumPublish = 0;

    @Basic
    @Column(name = "qd_sum_publish")
    private Integer qdSumPublish = 0;

    @Basic
    @Column(name = "miao_sum_publish")
    private Integer miaoSumPublish = 0;

    @Basic
    @Column(name = "tj_sum_success")
    private Integer tjSumSuccess = 0;

    @Basic
    @Column(name = "jz_sum_success")
    private Integer jzSumSuccess = 0;

    @Basic
    @Column(name = "lz_sum_success")
    private Integer lzSumSuccess = 0;

    @Basic
    @Column(name = "qd_sum_success")
    private Integer qdSumSuccess = 0;

    @Basic
    @Column(name = "miao_sum_success")
    private Integer miaoSumSuccess = 0;

    @Basic
    @Column(name = "tj_sum_repay")
    private Integer tjSumRepay = 0;

    @Basic
    @Column(name = "tj_sum_repay_principal")
    private Integer tjSumRepayPrincipal = 0;

    @Basic
    @Column(name = "jz_sum_repay")
    private Integer jzSumRepay = 0;

    @Basic
    @Column(name = "qd_sum_repay")
    private Integer qdSumRepay = 0;

    @Basic
    @Column(name = "qd_sum_repay_principal")
    private Integer qdSumRepayPrincipal = 0;

    @Basic
    @Column(name = "jz_sum_repay_principal")
    private Integer jzSumRepayPrincipal = 0;

    @Basic
    @Column(name = "tj_sum_repayment")
    private Long tjSumRepayment = 0l;

    @Basic
    @Column(name = "tj_sum_repayment_principal")
    private Long tjSumRepaymentPrincipal = 0l;

    @Basic
    @Column(name = "jz_sum_repayment")
    private Long jzSumRepayment = 0l;

    @Basic
    @Column(name = "qd_sum_repayment")
    private Long qdSumRepayment = 0l;

    @Basic
    @Column(name = "qd_sum_repayment_principal")
    private Long qdSumRepaymentPrincipal = 0l;

    @Basic
    @Column(name = "jz_sum_repayment_principal")
    private Long jzSumRepaymentPrincipal = 0l;

    @Basic
    @Column(name = "date")
    private Date date;
}
