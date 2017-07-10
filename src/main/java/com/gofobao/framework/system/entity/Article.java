package com.gofobao.framework.system.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by admin on 2017/5/31.
 */
@Entity(name = "Article")
@Table(name = "gfb_article")
@Data
public class Article {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "type", nullable = false, length = 20)
    private String type;
    @Basic
    @Column(name = "title", nullable = false, length = 255)
    private String title;
    @Basic
    @Column(name = "status", nullable = false)
    private Byte status;
    @Basic
    @Column(name = "author_id", nullable = false)
    private Integer authorId;
    @Basic
    @Column(name = "content", nullable = false, length = -1)
    private String content;
    @Basic
    @Column(name = "timeLimit", nullable = false)
    private Integer order;
    @Basic
    @Column(name = "created_at", nullable = true)
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at", nullable = true)
    private Timestamp updatedAt;

}
