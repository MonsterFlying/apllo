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
@Table(name = "gfb_marketing_redpack_record")
@DynamicUpdate
@DynamicInsert
@Data
public class MarketingRedpackRecord {
    @Id
    @GeneratedValue
    private Long id;
    private Long marketingId;
    private Long redpackRuleId;
    private Long userId;
    private Long sourceId;
    private Long money;
    private String markeingTitel;
    private Integer state;
    private Date publishTime;
    private Date openTime;
    private Date cancelTime;
    private Integer del;
    private String remark;

}
