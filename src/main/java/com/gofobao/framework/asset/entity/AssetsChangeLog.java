package com.gofobao.framework.asset.entity;

/**
 * Created by Administrator on 2017/7/8 0008.
 */


import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "gfb_assets_change_log")
@Entity
@Data
@DynamicInsert
@DynamicUpdate
public class AssetsChangeLog {
    @Id
    @GeneratedValue
    private Long id  ;
    private Long userId  ;
    private String accountId  ;
    private Long money  ;
    private Long useMoney  ;
    private Long noUseMoney  ;
    private Long virtualMoney  ;
    private Long collection  ;
    private Long payment  ;
    private Integer assetChangeType  ;
    private Integer del  ;
    private Integer synState  ;
    private Date createAt  ;
    private Date synTime  ;
    private Date updateAt  ;
    private String remark ;
}
