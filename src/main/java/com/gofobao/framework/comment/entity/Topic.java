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
@Table(name = "gfb_topics")
@DynamicInsert
@DynamicUpdate
@Data
public class Topic implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private Long topicTypeId;

    private Long userId;

    private String userName;

    private String userIconUrl;

    private Integer sort;

    private Integer fixState;

    private Integer hotState;

    private Integer newState;

    private Integer topTotalNum;

    private Integer contentTotalNum;

    private Integer viewTotalNum;

    private Integer del;

    private Date createDate;

    private Date updateDate;

    private String img1;

    private String img2;

    private String img3;

    private String img4;

    private String img5;

    private String img6;

    private String img7;

    private String img8;

    private String img9;

    private String content;

    public String getTitle() {
        if (!StringUtils.isEmpty(this.title)) {
            return EmojiParser.parseToUnicode(this.title);
        } else {
            return title;
        }
    }

    public void setTitle(String title) {
        if (!StringUtils.isEmpty(title)) {
            this.title = EmojiParser.parseToAliases(title);
        } else {
            this.title = title;
        }
    }

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
