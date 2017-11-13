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
@Table(name = "gfb_topics_comment")
@DynamicUpdate
@DynamicInsert
@Data
public class TopicComment implements Serializable {
    @Id
    @GeneratedValue
    private long id;

    private long topicId;

    private long topicTypeId;

    private String content;

    private long userId;

    private String userName;

    private String userIconUrl;

    private Integer topTotalNum;

    private Integer contentTotalNum;

    private Integer del;

    private Date createDate;

    private Date updateDate;


}
