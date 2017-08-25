package com.gofobao.framework.system.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

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
    private String errorMsg;
    private String remark;
    private Boolean isDel;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
