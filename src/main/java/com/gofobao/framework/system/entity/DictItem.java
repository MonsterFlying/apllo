package com.gofobao.framework.system.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Zeke on 2017/5/22.
 */
@Entity
@Data
@DynamicInsert
@Table(name = "gfb_dict_item")
public class DictItem {
    @Id
    @Column(name = "ID")
    private Long id;
    @Basic
    @Column(name = "NAME")
    private String name;
    @Basic
    @Column(name = "ALIAS_CODE")
    private String aliasCode;
    @Basic
    @Column(name = "IS_DEL")
    private Integer isDel;
    @Basic
    @Column(name = "CREATE_TIME")
    private Timestamp createTime;
    @Basic
    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;
    @Basic
    @Column(name = "CREATE_ID")
    private Integer createId;
    @Basic
    @Column(name = "UPDATE_ID")
    private Integer updateId;

}
