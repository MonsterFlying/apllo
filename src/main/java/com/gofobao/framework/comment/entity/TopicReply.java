package com.gofobao.framework.comment.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

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
@Table(name = "gfb_topics_reply")
@DynamicInsert
@DynamicUpdate
@Data
public class TopicReply implements Serializable {
    @Id
    @GeneratedValue
    private Integer id;

    private long topicId;

    private long topicCommentId;

    private long topicTypeId;

    private long topicReplyId;

    private Integer replyType;

    private String content;

    private Integer topTotalNum;

    private Integer userId;

    private String userName;

    private String userIconUrl;

    private Integer forUserId;

    private String forUserName;

    private String forUserIconUrl;

    private Integer del;

    private Date createDate;

    private Date updateDate;

}
