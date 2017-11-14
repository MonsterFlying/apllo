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
    private Long id;

    private Long topicId;

    private Long topicCommentId;

    private Long topicTypeId;

    private Long topicReplyId;

    private Integer replyType;

    private String content;

    private Integer topTotalNum;

    private Long userId;

    private String userName;

    private String userIconUrl;

    private Long forUserId;

    private String forUserName;

    private String forUserIconUrl;

    private Integer del;

    private Date createDate;

    private Date updateDate;

}
