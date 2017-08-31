package com.gofobao.framework.financial.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "gfb_financial_scheduler")
@Entity
@DynamicInsert
@DynamicUpdate
public class FinancialScheduler {
    @GeneratedValue
    @Id
    private Long id  ;
    private String name  ;
    private String data  ;
    private Integer doNum  ;
    private String resMsg  ;
    private Date createAt  ;
    private Date updateAt  ;
    private String type ;
    private Integer state  ;
}
