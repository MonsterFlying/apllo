package com.gofobao.framework.asset.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/6/7.
 */
@Entity
@Data
@Table(name = "gfb_advance_log")
public class AdvanceLog {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    private Long userId;
    private Integer status;
    private Date advanceAtYes;
    private Integer advanceMoneyYes;
    private Date repayAtYes;
    private Integer repayMoneyYes;
}
