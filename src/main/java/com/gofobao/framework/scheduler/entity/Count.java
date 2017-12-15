package com.gofobao.framework.scheduler.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by xin on 2017/12/7.
 */
@Entity
@Table(name = "gfb_count")
@DynamicUpdate
@DynamicInsert
@Data
public class Count {
    @Id
    @GeneratedValue
    private long id;

    private String siteBalance;

    private String accountBalance;

    private Long netWaitCollectionPrincipal;

    private Long netWaitCollectionInterest;

    private Long netWaitRepaymentPrincipal;

    private Long netWaitRepaymentInterest;

    private Long netAdvanceNoPrincipal;

    private Long netAdvanceNoInterest;

    private String carWaitCollection;

    private String carWaitRepayment;

    private String channelWaitCollection;

    private String channelWaitRepayment;

    private Long netBorrowPrincipal;

    private Long netRepaymentPrincipal;

    private Long netAdvancePrincipal;

    private Long netAdvanceYesPrincipal;

    private Long netNetAdvancePrincipal;

    private Long netNetIncreasePrincipal;

    private Long WaitCollectionPrincipal;

    private String carBorrowPrincipal;

    private String carRepaymentPrincipal;

    private String carIncreasePrincipal;

    private String carWaitCollectionPrincipal;

    private String channelBorrowPrincipal;

    private String channelRepaymentPrincipal;

    private String channelIncreasePrincipal;

    private String channelWaitCollectionPrincipal;

    private Date createTime;

    private Date updateTime;

    private Date countDate;
}
