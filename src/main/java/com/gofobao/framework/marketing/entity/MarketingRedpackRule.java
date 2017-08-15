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
@Table(name = "gfb_marketing_redpack_rule")
@DynamicUpdate
@DynamicInsert
@Data
public class MarketingRedpackRule {
    @Id
    @GeneratedValue
    private Long id;
    private Long marketingId;
    private Integer ruleType;
    private Double tenderMoneyMin;
    private Double tenderMoneyMax;
    private Long moneyMin;
    private Long moneyMax;
    private Integer apr;
    private Integer del;
    private Date createTime;
}
