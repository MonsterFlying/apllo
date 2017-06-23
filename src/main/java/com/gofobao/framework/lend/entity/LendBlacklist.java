package com.gofobao.framework.lend.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/26.
 */
@Entity
@Data
@DynamicInsert
@Table(name = "gfb_lend_blacklist")
public class LendBlacklist {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "black_user_id")
    private Long blackUserId;
    @Basic
    @Column(name = "user_id")
    private Long userId;
    @Basic
    @Column(name = "created_at")
    private Date createdAt;
}
