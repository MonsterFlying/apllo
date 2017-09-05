package com.gofobao.framework.system.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Zeke on 2017/8/25.
 */
@Entity
@Table(name = "gfb_third_error_remark")
@Data
public class ThirdErrorRemark {
    @Id
    @GeneratedValue
    private Long id;
    private Integer state;
    private Long userId;
    private Integer type;
    private Long sourceId;
    private Long toUserId;
    private String thirdReqStr;
    private String thirdRespStr;
    private String thirdErrorMsg;
    private String errorMsg;
    private String oldBatchNo;
    private String remark;
    private Boolean isDel;
    private Date createdAt;
    private Date updatedAt;
}
