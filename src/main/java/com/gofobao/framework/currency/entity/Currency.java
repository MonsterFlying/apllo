package com.gofobao.framework.currency.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/22.
 */
@Data
@Entity
@Table(name = "gfb_currency")
public class Currency {
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Basic
    @Column(name = "use_currency")
    private Integer useCurrency;
    @Basic
    @Column(name = "no_use_currency")
    private Integer noUseCurrency;
    @Basic
    @Column(name = "updated_at")
    private Date updatedAt;

}
