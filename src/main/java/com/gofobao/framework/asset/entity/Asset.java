package com.gofobao.framework.asset.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/19.
 */
@Entity
@DynamicInsert
@Table(name = "gfb_asset")
@Data
public class Asset {
    @Id
    @Column(name = "user_id")
    //@GeneratedValue
    private Long userId;

    private Long useMoney;

    private Long noUseMoney;

    private Long virtualMoney;

    private Long collection;

    private Long payment;

    private Date updatedAt;

    private Long financePlanMoney;

    public long getTotal() {
        return this.useMoney + this.noUseMoney + this.collection;
    }
}
