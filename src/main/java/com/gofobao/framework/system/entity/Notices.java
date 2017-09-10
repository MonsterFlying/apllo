package com.gofobao.framework.system.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Max on 17/6/5.
 */
@Entity
@Table(name = "gfb_notices")
@DynamicUpdate
@DynamicInsert
@Data
public class Notices {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long fromUserId;

    private Long userId;

    @Column(name = "`read`")
    @Basic
    private Boolean read;

    private String name;

    @Column(name = "`content`")
    @Basic
    private String content;

    private String type;

    private Date createdAt;

    private Date updatedAt;

    private Date deletedAt;
}
