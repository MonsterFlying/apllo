package com.gofobao.framework.borrow.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * Created by admin on 2017/5/31.
 */
@Entity
@Data
@Table(name = "gfb_borrow_virtual")
@DynamicUpdate
@DynamicInsert
public class BorrowVirtual {
    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    private String name;

    private Integer status;

    private Integer apr;

    private Integer timeLimit;

    private Integer lowest;

    private Integer most;

    private Integer awardType;

    private Integer award;

    private String description;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
