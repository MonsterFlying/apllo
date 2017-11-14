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
@Table(name = "gfb_topics_report")
@DynamicInsert
@DynamicUpdate
@Data
public class TopicReport implements Serializable {
    @Id
    @GeneratedValue
    private long id;
    private Long sourceId;
    private Integer sourceType;
    private Integer reportType;
    private Long userId;
    private Long reportUserId;
    private Integer responseState;
    private String responseContent;
    private Date responseDate;
    private Date createDate;
    private Date updateDate;
}
