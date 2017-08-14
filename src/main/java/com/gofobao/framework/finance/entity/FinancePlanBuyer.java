package com.gofobao.framework.finance.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Zeke on 2017/8/10.
 */
@Entity
@Table(name = "gfb_finance_plan_buyer")
@Data
@DynamicInsert
@DynamicUpdate
public class FinancePlanBuyer {
    @Id
    @GeneratedValue
    private Long id;
    private Long planId;
    private Long userId;
    private Integer status;
    private Integer baseApr;
    private Integer apr;
    private Long money;
    private Long validMoney;
    private Long rightMoney;
    private Long leftMoney;
    private Date endLockAt;
    private Integer finishedState;
    private Integer source;
    private String remark;
    private String freezeOrderId;
    private Date createdAt;
    private Date updatedAt;
}
