package com.gofobao.framework.comment.entity;

import com.vdurmont.emoji.EmojiParser;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
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
@Table(name = "gfb_topics_notices ")
@DynamicInsert
@DynamicUpdate
@Data
public class TopicsNotices implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private Long sourceId;
    private Integer sourceType;
    private Long forUserId;
    private String forUserName;
    private String content;
    private String forUserIconUrl;
    private Integer viewState;
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
