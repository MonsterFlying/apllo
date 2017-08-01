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
    private Long principal;
    private Integer alreadyInterest;
    private Integer leftOrder;
    private Long leftPrincipal;
    private Integer apr;
    private Date repayAt;
    private Long tenderId;
    private Long borrowId;
    private Long userId;
    private Long lower;
    private Boolean del;
    private Date createdAt;
    private Date updatedAt;

}
