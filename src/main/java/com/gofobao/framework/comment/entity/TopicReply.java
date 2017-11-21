package com.gofobao.framework.comment.entity;

import com.vdurmont.emoji.EmojiParser;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;

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

    public String getContent() {
        if (!StringUtils.isEmpty(this.content)) {
            return EmojiParser.parseToUnicode(this.content);
        } else {
            return content;
        }
    }

    public void setContent(String content) {
        if (!StringUtils.isEmpty(content)) {
            this.content = EmojiParser.parseToAliases(content);
        } else {
            this.content = content;
        }
    }

}
