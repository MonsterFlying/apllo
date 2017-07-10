package com.gofobao.framework.system.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by admin on 2017/5/31.
 */
@Entity(name = "Banner")
@Table(name = "gfb_banner")
@Data
public class Banner {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic
    @Column(name = "status", nullable = false)
    private Byte status;
    @Basic
    @Column(name = "terminal", nullable = false)
    private Integer terminal;
    @Basic
    @Column(name = "title", nullable = false, length = 20)
    private String title;
    @Basic
    @Column(name = "imgurl", nullable = false, length = 100)
    private String imgurl;
    @Basic
    @Column(name = "clickurl", nullable = false, length = 100)
    private String clickurl;
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
