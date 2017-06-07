package com.gofobao.framework.member.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by admin on 2017/5/31.
 */
@Entity
@Data
@Table(name = "gfb_broker_bouns")
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class BrokerBouns {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Basic
    @Column(name = "level", nullable = false)
    private Integer level;
    @Basic
    @Column(name = "award_apr", nullable = false)
    private Short awardApr;

    @Basic
    @Column(name = "wait_principal_total", nullable = false)
    private Long waitPrincipalTotal;

    @Basic
    @Column(name = "bouns_award", nullable = false)
    private Integer bounsAward;

    @Basic
    @Column(name = "created_at", nullable = true)
    private Date createdAt;


}
