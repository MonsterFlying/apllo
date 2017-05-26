package com.gofobao.framework.lend.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/26.
 */
@Entity
@Data
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
    @Column(name = "created_at")
    private Date createdAt;
}
