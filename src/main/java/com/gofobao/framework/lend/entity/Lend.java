package com.gofobao.framework.lend.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/26.
 */
@Entity
@Data
@DynamicInsert
@Table(name = "gfb_lend")
public class Lend {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "money")
    private Integer money;
    @Basic
    @Column(name = "status")
    private Integer status;
    @Basic
    @Column(name = "lowest")
    private Integer lowest;
    @Basic
    @Column(name = "apr")
    private Integer apr;
    @Basic
    @Column(name = "repay_fashion")
    private Integer repayFashion;
    @Basic
    @Column(name = "time_limit")
    private Integer timeLimit;
    @Basic
    @Column(name = "money_yes")
    private Integer moneyYes;
    @Basic
    @Column(name = "repay_at")
    private Date repayAt;
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
    @Basic
    @Column(name = "t_user_id")
    private Long tUserId;

}
