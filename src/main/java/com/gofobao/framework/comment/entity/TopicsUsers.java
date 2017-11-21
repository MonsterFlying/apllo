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
@Table(name = "gfb_topics_users")
@DynamicInsert
@DynamicUpdate
@Data
public class TopicsUsers implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private String username;
    private String avatar;
    private Integer forceState;
    private Long levelId;
    private Long useIntegral;
    private Long noUseIntegral;
    private Date createDate;
    private Date updateDate;
}
