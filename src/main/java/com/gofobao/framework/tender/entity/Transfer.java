package com.gofobao.framework.tender.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Zeke on 2017/7/31.
 */
@Entity
@Data
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
    private Date createdAt;
    private Date releaseAt;
    private Date updatedAt;
    private Date verifyAt;
    private Date successAt;
}
