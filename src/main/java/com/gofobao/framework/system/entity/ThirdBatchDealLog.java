package com.gofobao.framework.system.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Zeke on 2017/9/12.
 */
@Entity
@Table(name = "gfb_third_batch_deal_log")
@Data
@DynamicInsert
public class ThirdBatchDealLog {
    @Id
    @GeneratedValue
    private Long id;
    private String batchNo;
    private Integer state;
    private String errorMsg;
    private Integer type;
    private Date createdAt;
    private Date updatedAt;
}
