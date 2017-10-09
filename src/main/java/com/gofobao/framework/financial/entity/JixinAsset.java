package com.gofobao.framework.financial.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "jixin_asset")
@DynamicInsert
@DynamicUpdate
@Data
public class JixinAsset {
    @Id
    @GeneratedValue
    private Long id;
    private String accountId;
    private Long userId;
    private Date updateTime;
    private Long currMoney;
}
