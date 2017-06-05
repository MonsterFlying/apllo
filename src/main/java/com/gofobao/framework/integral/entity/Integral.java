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
@Table(name = "gfb_integral")
public class Integral {
    @Id
    @Column(name = "user_id")
    // @GeneratedValue
    private Long userId;
    @Basic
    @Column(name = "use_integral")
    private Integer useIntegral;
    @Basic
    @Column(name = "no_use_integral")
    private Integer noUseIntegral;
    @Basic
    @Column(name = "updated_at")
    private Date updatedAt;
}
