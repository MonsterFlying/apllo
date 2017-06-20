package com.gofobao.framework.asset.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/22.
 */
@Data
@Entity
@Table(name = "gfb_yesterday_asset")
@DynamicInsert
public class YesterdayAsset {
    @Id
    @Column(name = "user_id")
    private Long userId;
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
    @Column(name = "updated_at")
    private Date updatedAt;
}
