package com.gofobao.framework.message.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Max on 17/5/17.
 */
@Entity
@Table(name = "gfb_sms_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsConfigEntity {
    @Id
    @Column(name = "ID")
    private Long id;
    @Basic
    @Column(name = "ALIAS_CODE")
    private String aliasCode;
    @Basic
    @Column(name = "CONFIG")
    private String config;

    @Basic
    @Column(name = "CONFIG_NAME")
    private String configName;
    @Basic
    @Column(name = "IS_DEL")
    private Integer isDel;
    @Basic
    @Column(name = "IS_ACTIVE")
    private Integer isActive;
    @Basic
    @Column(name = "CREATE_TIME")
    private Date createTime;
    @Basic
    @Column(name = "UPDATE_TIME")
    private Date updateTime;

    @Basic
    @Column(name = "CREATE_ID")
    private Integer createId;
    @Basic
    @Column(name = "UPDATE_ID")
    private Integer updateId;
    @Basic
    @Column(name = "NOTE")
    private String note;
}
