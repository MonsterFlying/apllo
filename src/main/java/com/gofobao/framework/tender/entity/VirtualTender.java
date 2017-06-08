package com.gofobao.framework.tender.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by admin on 2017/5/31.
 */
@Entity(name = "VirtualTender")
@Data
@Table(name = "gfb_borrow_virtual_tender")
public class VirtualTender {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    @Basic
    @Column(name = "borrow_id", nullable = false)
    private Integer borrowId;
    @Basic
    @Column(name = "status", nullable = false)
    private Integer status;
    @Basic
    @Column(name = "money", nullable = false)
    private Integer money;
    @Basic
    @Column(name = "created_at", nullable = true)
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at", nullable = true)
    private Timestamp updatedAt;
}
