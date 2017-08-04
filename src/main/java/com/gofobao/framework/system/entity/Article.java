package com.gofobao.framework.system.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by admin on 2017/5/31.
 */
@Entity(name = "Article")
@Table(name = "gfb_article")
@Data
@DynamicUpdate
@DynamicInsert
public class Article {
    @Id
    @GeneratedValue
    private Long id;
    private String type;
    private String title;
    private Byte status;
    private Integer authorId;
    private String content;
    private Integer order;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String previewImg ;
}
