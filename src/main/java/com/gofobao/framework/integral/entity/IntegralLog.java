package com.gofobao.framework.integral.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/22.
 */
@Data
@Entity
@DynamicInsert
@Table(name = "gfb_integral_log")
public class IntegralLog {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;
    @Basic
    @Column(name = "user_id")
    private Long userId;
    @Basic
    @Column(name = "type")
    private String type;
    @Basic
    @Column(name = "value")
    private Long value;
    @Basic
    @Column(name = "use_integral")
    private Long useIntegral;
    @Basic
    @Column(name = "no_use_integral")
    private Long noUseIntegral;
    @Basic
    @Column(name = "created_at")
    private Date createdAt;

}
