package com.gofobao.framework.borrow.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by admin on 2017/5/31.
 */
@Entity(name = "BorrowVirtual")
@Data
@Table(name = "gfb_borrow_virtual")
public class BorrowVirtual {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;
    @Basic
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Basic
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    @Basic
    @Column(name = "status", nullable = false)
    private Integer status;
    @Basic
    @Column(name = "apr", nullable = false)
    private Integer apr;
    @Basic
    @Column(name = "time_limit", nullable = false)
    private Integer timeLimit;
    @Basic
    @Column(name = "lowest", nullable = false)
    private Integer lowest;
    @Basic
    @Column(name = "most", nullable = false)
    private Integer most;
    @Basic
    @Column(name = "award_type", nullable = false)
    private Integer awardType;
    @Basic
    @Column(name = "award", nullable = false)
    private Integer award;
    @Basic
    @Column(name = "description")
    private String description;
    @Basic
    @Column(name = "created_at", nullable = true)
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at", nullable = true)
    private Timestamp updatedAt;
}
