package com.gofobao.framework.system.entity;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by Zeke on 2017/9/12.
 */
@Data
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
