package com.gofobao.framework.asset.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/22.
 */
@Data
@Entity
@Table(name = "gfb_bank_account")
@AllArgsConstructor
@NoArgsConstructor
public class BankAccount {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;
    @Basic
    @Column(name = "user_id")
    private String userId;
    @Basic
    @Column(name = "trade_no")
    private String tradeNo;
    @Basic
    @Column(name = "account")
    private String account;
    @Basic
    @Column(name = "bank")
    private Integer bank;
    @Basic
    @Column(name = "phone")
    private String phone;
    @Basic
    @Column(name = "city")
    private String city;
    @Basic
    @Column(name = "branch")
    private String branch;
    @Basic
    @Column(name = "recharge_total")
    private Integer rechargeTotal;
    @Basic
    @Column(name = "cash_total")
    private Integer cashTotal;
    @Basic
    @Column(name = "is_default")
    private Boolean isDefault;
    @Basic
    @Column(name = "is_verify")
    private Boolean isVerify;
    @Basic
    @Column(name = "created_at")
    private Date createdAt;
    @Basic
    @Column(name = "updated_at")
    private Date updatedAt;
    @Basic
    @Column(name = "deleted_at")
    private Date deletedAt;

}
