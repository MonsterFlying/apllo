package com.gofobao.framework.member.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by admin on 2017/5/31.
 */
@Entity
@Data
@Table(name = "gfb_user_attachment")
public class UserAttachment {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Basic
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    @Basic
    @Column(name = "filesize", nullable = false, length = 10)
    private String filesize;
    @Basic
    @Column(name = "op_user_id", nullable = false)
    private Integer opUserId;
    @Basic
    @Column(name = "filepath", nullable = false, length = 100)
    private String filepath;
    @Basic
    @Column(name = "created_at", nullable = true)
    private Timestamp createdAt;
}
