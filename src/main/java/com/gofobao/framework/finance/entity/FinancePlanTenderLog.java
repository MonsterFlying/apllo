package com.gofobao.framework.finance.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/8/10.
 */
@Entity
@Table(name = "gfb_finance_plan_tender_log")
@Data
@DynamicInsert
@DynamicUpdate
public class FinancePlanTenderLog {
    @Id
    @GeneratedValue
    private Long id;
    private Long borrowId;
    private Long tenderId;
    private Long buyerId;
    private Long money;
    private Long leftMoney;
    private Long transferBuyId;
    private Integer transferFlag;
    private Integer finnishedState;
    private Date createdAt;
    private Date updatedAt;
}
