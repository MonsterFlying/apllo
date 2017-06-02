package com.gofobao.framework.asset.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by admin on 2017/5/22.
 */


@Entity
@Table(name = "gfb_asset_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class AssetLog {


    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;

    @Basic
    @Column(name = "user_id")
    private Long userId;


    @Basic
    @Column(name = "type")
    private String type;

    @Basic
    @Column(name = "money")
    private Integer money;


    @Basic
    @Column(name = "use_money")
    private Integer useMoney;


    @Basic
    @Column(name = "no_use_money")
    private Integer noUseMoney;


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
    private Long toUserId;

    @Basic
    @Column(name = "remark")
    private String remark;

    @Basic
    @Column(name = "created_at")
    private Date createdAt;


}
