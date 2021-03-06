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
@Table(name = "gfb_marketing")
@DynamicUpdate
@DynamicInsert
@Data
public class Marketing {
    @Id
    @GeneratedValue
    private Long id ;
    private Integer marketingType ;
    private String titel ;
    private String introduction ;
    private String targerUrl ;
    private String viewUrl ;
    private Date beginTime ;
    private Date endTime ;
    private Integer openState ;
    private Integer del ;
    private Date createTime ;
    private Date updateTime ;
    private Integer parentState ;
}
