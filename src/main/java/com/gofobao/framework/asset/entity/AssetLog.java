package com.gofobao.framework.asset.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.format.annotation.DateTimeFormat;

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
    @GeneratedValue
    private Long id;
    private Long userId;
    private String type;
    private Long money;
    private Long useMoney;
    private Long noUseMoney;
    private Long virtualMoney;
    private Long collection;
    private Long payment;
    private Long toUserId;
    private String remark;
    private Date createdAt;
}
