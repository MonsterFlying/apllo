package com.gofobao.framework.asset.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Zeke on 2017/6/7.
 */
@Entity
@Data
@DynamicInsert
@Table(name = "gfb_advance_log")
public class AdvanceLog {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private Long repaymentId;
    private Integer status;
    private Date advanceAtYes;
    private Long advanceMoneyYes;
    private Date repayAtYes;
    private Long repayMoneyYes;
}
