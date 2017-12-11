package com.gofobao.framework.comment.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by xin on 2017/11/8.
 */
@Entity
@Table(name = "gfb_topics_integral_record")
@DynamicInsert
@DynamicUpdate
@Data
public class TopicsIntegralRecord implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId ;
    private Integer opType ;
    private String opName ;
    private String opFlag ;
    private Long opMoney ;
    private Long useIntegral ;
    private Long noUseIntegral ;
    private Long sourceId ;
    private Integer sourceType ;
    private Integer del ;
    private Date createDate ;
    private Date updateDate ;
}
