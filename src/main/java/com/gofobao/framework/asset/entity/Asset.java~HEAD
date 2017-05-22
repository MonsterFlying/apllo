package com.gofobao.framework.asset.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
/**
 * Created by admin on 2017/5/22.
 */

@Entity
@Table(name = "gfb_asset")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Basic
    @Column(name = "use_money")
    private Integer userMoney;

    @Basic
    @Column(name = "no_use_money")
    private Integer noUserMoney;

    @Basic
    @Column(name = "virtual_money")
    private Integer virtualMoney;

    @Basic
    @Column(name = "collection")
    private Integer collection;

    @Basic
    @Column(name = "payment")
    private Integer payment;

    @Basic
    @Column(name = "updated_at")
    private Integer updatedAt;


}
