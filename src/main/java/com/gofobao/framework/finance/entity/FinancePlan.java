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
@Table(name = "gfb_finance_plan")
@Data
@DynamicInsert
@DynamicUpdate
public class FinancePlan {
    @Id
    @GeneratedValue
    private Long id;
    private Integer status;
    private String name;
    private Long money;
    private Long moneyYes;
    private Long rightMoney;
    private Long leftMoney;
    private Integer baseApr;
    private Integer timeLimit;
    private Integer lockPeriod;
    private Long lowest;
    private Integer appendMultipleAmount;
    private Long most;
    private Date successAt;
    private Date endLockAt;
    private Boolean finishedState;
    private Integer totalSubPoint;
    private Integer subPointCount;
    private Integer createId;
    private Integer updateId;
    private String description;
    private Date createdAt;
    private Date updatedAt;
    private Integer type;
}
