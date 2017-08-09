package com.gofobao.framework.asset.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "gfb_current_income_log")
public class CurrentIncomeLog {
    @Id
    @GeneratedValue
    private Long id;
    private Long money;
    private Long userId;
    private Date createAt;
    private Integer state;
    private String seqNo;
}
