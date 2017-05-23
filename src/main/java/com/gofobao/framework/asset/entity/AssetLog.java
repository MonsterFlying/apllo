package com.gofobao.framework.asset.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Created by admin on 2017/5/22.
 */


@Entity
@Table(name = "gfb_asset_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetLog {


    @Id
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "user_id")
    private Long userId;


    @Basic
    @Column(name = "type")
    private Integer type;

    @Basic
    @Column(name = "money")
    private Integer money;


    @Basic
    @Column(name = "use_money")
    private Integer useMoney;


    @Basic
    @Column(name = "no_use_money")
    private Integer no_use_money;


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
    @Column(name = "to_user_id")
    private Integer toUserId;

    @Basic
    @Column(name = "remark")
    private Integer remark;

    @Basic
    @Column(name = "created_at")
    private Integer createdAt;


}
