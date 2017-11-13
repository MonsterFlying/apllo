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
@Table(name = "gfb_topics")
@DynamicInsert
@DynamicUpdate
@Data
public class Topic implements Serializable {
    @Id
    @GeneratedValue
    private long id;

    private String title;

    private long topicTypeId;

    private long userId;

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


}
