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
@Table(name = "gfb_topics_top_record")
@DynamicUpdate
@DynamicInsert
@Data
public class TopicTopRecord implements Serializable {
    @Id
    @GeneratedValue
    private long id;

    private Integer userId;

    private Integer sourceid;

    private Integer sourceType;

    private Date createDate;

    private Date updateDate;
}
