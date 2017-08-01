package com.gofobao.framework.tender.entity;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Zeke on 2017/7/31.
 */
@Entity
@Table(name = "gfb_transfer_buy_log")
@Data
public class TransferBuyLog {
    @GeneratedValue
    @Id
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
    private Date createdAt;
    private Date updatedAt;

}
