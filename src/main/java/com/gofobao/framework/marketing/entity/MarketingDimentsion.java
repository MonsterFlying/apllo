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
@Table(name = "gfb_marketing_dimension")
@DynamicUpdate
@DynamicInsert
@Data
public class MarketingDimentsion {
    @Id
    @GeneratedValue
    private Long id;
    private Long marketingId;
    private String platform;
    private String borrowType;
    private Integer memberType;
    private String channelType;
    private Integer parentState;
    private Integer del;
    private Date createTime;
}
