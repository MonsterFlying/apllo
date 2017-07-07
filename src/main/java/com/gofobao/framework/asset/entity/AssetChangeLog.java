package com.gofobao.framework.asset.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Time;
import java.util.Date;

/**
 * Created by Administrator on 2017/7/7 0007.
 */
@Table
@Entity
@Data
@DynamicUpdate
@DynamicInsert
public class AssetChangeLog {
    @Id
    @GeneratedValue
    private Long id ;
    private Long userId;
    private Long money ;
    private Integer type ;
    private Long availableMoney ;
    private Long feeMoney ;
    private Long virtual_money ;
    private Long collectionMoney ;
    private Long paymentMoney ;
    private Long forUserId ;
    private String remark ;
    private Date synchronizeAt ;
    private Integer synchronizeState ;
    private String  jixinSeqNo ;
    private String  jixinTxType ;
    private Date jixinTxDate ;
    private Time jixinTxTime ;
    private Date createAt;
    private Date updateAt ;
    private Long refId ;
    private String extendInfo ;
}
