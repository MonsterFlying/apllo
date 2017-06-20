package com.gofobao.framework.system.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Zeke on 2017/6/15.
 */
@Entity
@Table(name = "gfb_third_batch_log")
@Data
@DynamicInsert
public class ThirdBatchLog {
    @Id
    @GeneratedValue
    @Column
    private Long id;
    private String batchNo;
    private Integer type;
    private Long sourceId;
    private String remark;
    private Integer iparam1;
    private Integer iparam2;
    private Integer iparam3;
    private String vparam1;
    private String vparam2;
    private String vparam3;
    private Date createAt;
    private Date updateAt;
}
