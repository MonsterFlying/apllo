package com.gofobao.framework.tender.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;


/**
 * Created by admin on 2017/5/31.
 */
@Entity(name = "VirtualTender")
@Data
@Table(name = "gfb_borrow_virtual_tender")
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class VirtualTender {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    private Integer id;
    @Basic
    @Column(name = "user_id", nullable = false)
    private Long userId;
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
    private Date createdAt;
    @Basic
    @Column(name = "updated_at", nullable = true)
    private Date updatedAt;
}
