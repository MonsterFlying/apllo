package com.gofobao.framework.system.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Zeke on 2017/5/22.
 */
@Entity
@DynamicInsert
@Data
@Table(name = "gfb_dict_value")
public class DictValue {
    @Id
    @Column(name = "ID")
    private Long id;
    @Basic
    @Column(name = "ITEM_ID")
    private Long itemId;
    @Basic
    @Column(name = "VALUE01")
    private String value01;
    @Basic
    @Column(name = "VALUE02")
    private String value02;
    @Basic
    @Column(name = "VALUE03")
    private String value03;
    @Basic
    @Column(name = "VALUE04")
    private String value04;
    @Basic
    @Column(name = "VALUE05")
    private String value05;
    @Basic
    @Column(name = "VALUE06")
    private String value06;
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
    @Column(name = "IS_DEL")
    private Boolean isDel;
    @Basic
    @Column(name = "NAME")
    private String name;

}
