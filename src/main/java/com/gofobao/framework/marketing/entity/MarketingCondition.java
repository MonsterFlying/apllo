package com.gofobao.framework.marketing.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "gfb_marketing_condition")
@DynamicUpdate
@DynamicInsert
@Data
public class MarketingCondition {
    @Id
    @GeneratedValue
    private Long id;
    private Long marketingId;
    private Long tenderMoneyMin;
    private Date registerMinTime;
    private Date openAccountMinTime;
    private Long rechargeMoneyMin ;
    private Date loginMinTime ;
    private Integer del;
    private Date createTime;
}
