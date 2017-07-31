package com.gofobao.framework.tender.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Zeke on 2017/7/31.
 */
@Entity
@Data
@Table(name = "gfb_transfer")
public class Transfer {
    @GeneratedValue
    @Id
    private int id;
    private Integer state;
    private String title;
    private Integer principal;
    private Integer alreadyInterest;
    private Integer leftOrder;
    private Long leftPrincipal;
    private Integer apr;
    private Timestamp repayAt;
    private Integer tenderId;
    private Integer borrowId;
    private Integer userId;
    private Integer del;
    private Timestamp createdAt;
    private Timestamp updatedAt;

}
