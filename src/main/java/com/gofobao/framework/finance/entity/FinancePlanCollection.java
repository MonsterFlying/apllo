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
@Table(name = "gfb_finance_plan_collection")
@Data
@DynamicInsert
@DynamicUpdate
public class FinancePlanCollection {
    @Id
    @GeneratedValue
    private Long id;
    private Integer orderNum;
    private Integer status;
    private Long principal;
    private Long interest;
    private Date startAt;
    private Date collectionAt;
    private Date createdAt;
    private Date updatedAt;
}
