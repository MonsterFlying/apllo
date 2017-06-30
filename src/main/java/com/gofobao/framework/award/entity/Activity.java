package com.gofobao.framework.award.entity;

import lombok.Data;

import javax.persistence.*;
import java.sql.Date;

/**
 * Created by admin on 2017/5/31.
 */
@Data
@Entity(name = "Activity")
@Table(name = "gfb_activity")
public class Activity {

    @Id
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Basic
    @Column(name = "TITLE")
    private String title;


    @Basic
    @Column(name = "IS_OPEN")
    private Integer isOpen;

    @Basic
    @Column(name = "BEGIN_AT")
    private Date beginAt;

    @Basic
    @Column(name = "END_AT")
    private Date endAt;

    @Basic
    @Column(name = "DEL")
    private Integer del;
    @Basic
    @Column(name = "CREATE_AT")
    private Date createAt;
    @Basic
    @Column(name = "CREATE_BY")
    private Integer createBy;
    @Basic
    @Column(name = "UPDATE_AT")
    private Date updateAt;
    @Basic
    @Column(name = "UPDATE_BY")
    private Integer updateBy;
    @Basic
    @Column(name = "IPARAM1")
    private Integer iparam1;
    @Basic
    @Column(name = "IPARAM2")
    private Integer iparam2;
    @Basic
    @Column(name = "IPARAM3")
    private Integer iparam3;
    @Basic
    @Column(name = "VPARAM1")
    private String vparam1;
    @Basic
    @Column(name = "VPARAM2")
    private String vparam2;
    @Basic
    @Column(name = "VPARAM3")
    private String vparam3;
    @Basic
    @Column(name = "TYPE")
    private Integer type;
    @Basic
    @Column(name = "MIN")
    private Integer min;
    @Basic
    @Column(name = "MAX")
    private Integer max;

}
