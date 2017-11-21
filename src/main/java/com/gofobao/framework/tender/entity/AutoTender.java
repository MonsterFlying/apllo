package com.gofobao.framework.tender.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/27.
 */
@Entity
@DynamicInsert
@Table(name = "gfb_auto_tender")
@Data
@DynamicUpdate
public class AutoTender {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "user_id")
    private Long userId;
    @Basic
    @Column(name = "status")
    private Boolean status;
    @Basic
    @Column(name = "buyMoney")
    private Integer lowest;
    @Basic
    @Column(name = "borrow_types")
    private Integer borrowTypes;
    @Basic
    @Column(name = "tender_0")
    private Integer tender0;
    @Basic
    @Column(name = "tender_1")
    private Integer tender1;
    @Basic
    @Column(name = "tender_3")
    private Integer tender3;
    @Basic
    @Column(name = "tender_4")
    private Integer tender4;
    @Basic
    @Column(name = "mode")
    private Integer mode;
    @Basic
    @Column(name = "tender_money")
    private Integer tenderMoney;
    @Basic
    @Column(name = "repay_fashions")
    private Integer repayFashions;
    @Basic
    @Column(name = "timelimit_type")
    private Integer timelimitType;
    @Basic
    @Column(name = "timelimit_first")
    private Integer timelimitFirst;
    @Basic
    @Column(name = "timelimit_last")
    private Integer timelimitLast;
    @Basic
    @Column(name = "apr_first")
    private Integer aprFirst;
    @Basic
    @Column(name = "apr_last")
    private Integer aprLast;
    @Basic
    @Column(name = "save_money")
    private Integer saveMoney;
    @Basic
    @Column(name = "`order`")
    private Integer order;
    @Basic
    @Column(name = "auto_at")
    private Date autoAt;
    @Basic
    @Column(name = "created_at")
    private Date createdAt;
    @Basic
    @Column(name = "updated_at")
    private Date updatedAt;
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
}
