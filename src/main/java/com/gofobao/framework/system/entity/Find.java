package com.gofobao.framework.system.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "gfb_find")
@DynamicInsert
@DynamicUpdate
public class Find {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "`status`")
    private Short status;
    private String title;
    private String icon;
    private String url;
    @Column(name = "`order`")
    private String order ;
    private Long createId;
    private Long updateId;
    private Date createdAt;
    private Date updatedAt;
}
