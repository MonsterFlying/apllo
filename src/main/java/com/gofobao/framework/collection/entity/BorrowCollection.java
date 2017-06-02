package com.gofobao.framework.collection.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/26.
 */
@Entity
@Data
@Table(name = "gfb_borrow_collection")
@DynamicInsert
public class BorrowCollection {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "status")
    private Integer status;
    @Basic
    @Column(name = "`order`")
    private Integer order;

    @Basic
    @Column(name = "tender_id")
    private Long tenderId;

    @Basic
    @Column(name = "collection_money")
    private Integer collectionMoney;

    @Basic
    @Column(name = "principal")
    private Integer principal;

    @Basic
    @Column(name = "interest")
    private Integer interest;

    @Basic
    @Column(name = "start_at")
    private Date startAt;

    @Basic
    @Column(name = "start_at_yes")
    private Date startAtYes;

    @Basic
    @Column(name = "collection_at")
    private Date collectionAt;

    @Basic
    @Column(name = "collection_at_yes")
    private Date collectionAtYes;

    @Basic
    @Column(name = "collection_money_yes")
    private Integer collectionMoneyYes;

    @Basic
    @Column(name = "late_days")
    private Integer lateDays;
    @Basic
    @Column(name = "late_interest")
    private Integer lateInterest;
    @Basic
    @Column(name = "transfer_flag")
    private Integer transferFlag;
    @Basic
    @Column(name = "created_at")
    private Date createdAt;
    @Basic
    @Column(name = "updated_at")
    private Date updatedAt;
    @Basic
    @Column(name = "borrow_id")
    private Integer borrowId;
    @Basic
    @Column(name = "user_id")
    private Integer userId;
    @Basic
    @Column(name = "iparam1")
    private Integer iparam1;
    @Basic
    @Column(name = "iparam2")
    private Integer iparam2;
    @Basic
    @Column(name = "iparam3")
    private Integer iparam3;
    @Basic
    @Column(name = "vparam1")
    private String vparam1;
    @Basic
    @Column(name = "vparam2")
    private String vparam2;
    @Basic
    @Column(name = "vparam3")
    private String vparam3;
    @Basic
    @Column(name = "t_user_id")
    private Long tUserId;

}
