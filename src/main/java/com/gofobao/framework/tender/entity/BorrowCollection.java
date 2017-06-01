package com.gofobao.framework.tender.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by admin on 2017/5/31.
 */
@Entity
@Table(name = "gfb_borrow_collection")
public class BorrowCollection {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "user_id", nullable = true)
    private Integer userId;
    @Basic
    @Column(name = "borrow_id", nullable = true)
    private Integer borrowId;
    @Basic
    @Column(name = "status", nullable = false)
    private Integer status;
    @Basic
    @Column(name = "order", nullable = false)
    private Short order;
    @Basic
    @Column(name = "tender_id", nullable = false)
    private Integer tenderId;
    @Basic
    @Column(name = "collection_money", nullable = false)
    private Integer collectionMoney;
    @Basic
    @Column(name = "principal", nullable = false)
    private Integer principal;
    @Basic
    @Column(name = "interest", nullable = false)
    private Integer interest;
    @Basic
    @Column(name = "start_at", nullable = true)
    private Timestamp startAt;
    @Basic
    @Column(name = "start_at_yes", nullable = true)
    private Timestamp startAtYes;
    @Basic
    @Column(name = "collection_at", nullable = true)
    private Timestamp collectionAt;
    @Basic
    @Column(name = "collection_at_yes", nullable = true)
    private Timestamp collectionAtYes;
    @Basic
    @Column(name = "collection_money_yes", nullable = false)
    private Integer collectionMoneyYes;
    @Basic
    @Column(name = "late_days", nullable = false)
    private Short lateDays;
    @Basic
    @Column(name = "late_interest", nullable = false)
    private Integer lateInterest;
    @Basic
    @Column(name = "transfer_flag", nullable = false)
    private Integer transferFlag;
    @Basic
    @Column(name = "created_at", nullable = true)
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at", nullable = true)
    private Timestamp updatedAt;
    @Basic
    @Column(name = "iparam1", nullable = true)
    private Integer iparam1;
    @Basic
    @Column(name = "iparam2", nullable = true)
    private Integer iparam2;
    @Basic
    @Column(name = "iparam3", nullable = true)
    private Integer iparam3;
    @Basic
    @Column(name = "vparam1", nullable = true)
    private String vparam1;
    @Basic
    @Column(name = "vparam2", nullable = true)
    private String vparam2;
    @Basic
    @Column(name = "vparam3", nullable = true)
    private String vparam3;
    @Basic
    @Column(name = "t_user_id", nullable = true)
    private Integer tUserId;


}
