package com.gofobao.framework.comment.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by xin on 2017/11/8.
 */
@Entity
@Table(name = "gfb_topic_type")
@DynamicInsert
@DynamicUpdate
@Data
public class TopicType implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    private String topicTypeName;

    private Integer sort;

    private Integer hotState;

    private Integer newState;

    private Integer topicTotalNum;

    private String iconUrl;

    private Date createDate;

    private Date updateDate;

    private long adminId;

    private Integer del;

}
