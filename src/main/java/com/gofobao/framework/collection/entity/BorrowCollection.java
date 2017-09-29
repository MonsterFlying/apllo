package com.gofobao.framework.collection.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/26.
 */
@Entity(name = "BorrowCollection")
@Data
@Table(name = "gfb_borrow_collection")
@DynamicInsert
@DynamicUpdate
public class BorrowCollection {
    @Id
    @GeneratedValue
    private Long id;
    private Integer status;
    @Basic
    @Column(name = "`order`")
    private Integer order;
    private Long tenderId;
    private Long collectionMoney;
    private Long principal;
    private Long interest;
    private Date startAt;
    private Date startAtYes;
    private Date collectionAt;
    private Date collectionAtYes;
    private Long collectionMoneyYes;
    private Integer lateDays;
    private Long lateInterest;
    private Integer transferFlag;
    private Date createdAt;
    private Date updatedAt;
    private Long borrowId;
    private Long userId;
    private Long tUserId;
    private String tRepayOrderId;
    private Boolean thirdRepayFlag = false;
}
