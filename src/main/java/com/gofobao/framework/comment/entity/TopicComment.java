package com.gofobao.framework.comment.entity;

import cn.jiguang.common.utils.StringUtils;
import com.vdurmont.emoji.EmojiParser;
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
    private Long id;

    private Long topicId;

    private Long topicTypeId;

    private String content;

    private Long userId;

    private String userName;

    private String userIconUrl;

    private Integer topTotalNum;

    private Integer contentTotalNum;

    private Integer del;

    private Date createDate;

    private Date updateDate;

    public String getContent() {
        if (!org.springframework.util.StringUtils.isEmpty(this.content)) {
            return EmojiParser.parseToUnicode(this.content);
        } else {
            return content;
        }
    }

    public void setContent(String content) {
        if (!org.springframework.util.StringUtils.isEmpty(content)) {
            this.content = EmojiParser.parseToAliases(content);
        } else {
            this.content = content;
        }
    }

}
