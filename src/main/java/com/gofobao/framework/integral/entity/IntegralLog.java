package com.gofobao.framework.integral.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/22.
 */
@Data
@Entity
@Table(name = "gfb_integral_log")
public class IntegralLog {
    @Id
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "type")
    private String type;
    @Basic
    @Column(name = "value")
    private Integer value;
    @Basic
    @Column(name = "use_integral")
    private Integer useIntegral;
    @Basic
    @Column(name = "no_use_integral")
    private Integer noUseIntegral;
    @Basic
    @Column(name = "created_at")
    private Date createdAt;

}
