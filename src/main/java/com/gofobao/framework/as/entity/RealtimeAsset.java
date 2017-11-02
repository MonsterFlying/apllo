package com.gofobao.framework.as.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/22.
 */
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "gfb_realtime_asset")
public class RealtimeAsset {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private Long accountId;
    private String username;
    private String phone;
    private Double jixinTotalAmount;
    private Double localTotalAmount;
    private Double jixinUseAmount;
    private Double localUseAmount;
    private Double intevalMoney;
    private Date createTime;
    private Long batchNo;
}
