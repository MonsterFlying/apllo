package com.gofobao.framework.tender.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Zeke on 2017/7/31.
 */
@Entity
@Table(name = "gfb_transfer_buy_log")
@DynamicInsert
@DynamicUpdate
@Data
public class TransferBuyLog {
    @Id
    @GeneratedValue
    private Long id;
    private Integer state;
    private Long transferId;
    private Long userId;
    private Long buyMoney;
    private Long validMoney;
    private Long alreadyInterest;
    private Boolean del;
    private Boolean auto;
    private Integer autoOrder;
    private Integer source;
    private Long principal;
    private Date createdAt;
    private Date updatedAt;
    private Boolean thirdTransferFlag;
    private String thirdTransferOrderId;
    private String transferAuthCode;
    private String freezeOrderId;
}
