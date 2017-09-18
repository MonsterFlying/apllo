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
@Data
@DynamicInsert
@DynamicUpdate
@Table(name = "gfb_transfer")
public class Transfer {
    @GeneratedValue
    @Id
    private Long id;
    private Integer state;
    private String title;
    private Long transferMoney;
    private Long principal;
    private Long alreadyInterest;
    private Integer timeLimit;
    private Long transferMoneyYes;
    private Integer apr;
    private Date repayAt;
    private Long tenderId;
    private Boolean isLock;
    private Integer tenderCount;
    private Long borrowId;
    private Long userId;
    private Long lowest;
    private Boolean del;
    private Boolean isAll = true;
    private Integer startOrder;
    private Integer endOrder;
    private String borrowCollectionIds;
    private Date createdAt;
    private Date releaseAt;
    private Date updatedAt;
    private Date verifyAt;
    private Date successAt;
    private Integer type;
    private Date recheckAt;
}
