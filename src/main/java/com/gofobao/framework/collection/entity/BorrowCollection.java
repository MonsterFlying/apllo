package com.gofobao.framework.collection.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/26.
 */
@Entity(name = "BorrowCollection")
@Data
@Table(name = "gfb_borrow_collection")
@DynamicInsert
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
    private Integer iparam1;
    private Integer iparam2;
    private Integer iparam3;
    private String vparam1;
    private String vparam2;
    private String vparam3;
    private Long tUserId;
    private String tRepayOrderId;
    private String tBailRepayOrderId;
    private String tRepayBailOrderId;
    private String tBailAuthCode;
    private Boolean thirdRepayFlag;
    private Boolean thirdRepayBailFlag; //第三方是否借款人还担保人 0否 1是
    private Boolean thirdBailRepayFlag; //第三方是否登记担保人垫付 0否 1是
}
