package com.gofobao.framework.award.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;


/**
 * Created by admin on 2017/5/31.
 */
@Entity(name ="Coupon")
@Table(name = "gfb_coupon")
@Data
public class Coupon {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Basic
    @Column(name = "status", nullable = false)
    private Integer status;
    @Basic
    @Column(name = "type", nullable = false)
    private Integer type;
    @Basic
    @Column(name = "start_at", nullable = true)
    private Date startAt;
    @Basic
    @Column(name = "end_at", nullable = true)
    private Date endAt;

    @Basic
    @Column(name = "phone", nullable = false, length = 15)
    private String phone;
    @Basic
    @Column(name = "size", nullable = false, length = 10)
    private String size;
    @Basic
    @Column(name = "created_at", nullable = true)
    private Date createdAt;
    @Basic
    @Column(name = "updated_at", nullable = true)
    private Date updatedAt;


}
