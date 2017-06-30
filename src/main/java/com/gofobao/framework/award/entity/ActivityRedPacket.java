package com.gofobao.framework.award.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;


/**
 * Created by admin on 2017/5/31.
 */
@Entity(name ="ActivityRedPacket")
@Table(name = "gfb_activity_red_packet")
@Data
public class ActivityRedPacket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Integer id;
    @Basic
    @Column(name = "ACTIVITY_ID", nullable = true)
    private Integer activityId;
    @Basic
    @Column(name = "ACTIVITY_NAME", nullable = true, length = 255)
    private String activityName;
    @Basic
    @Column(name = "STATUS", nullable = true)
    private Integer status;
    @Basic
    @Column(name = "USER_ID", nullable = true)
    private Long userId;
    @Basic
    @Column(name = "USER_NAME", nullable = true, length = 24)
    private String userName;
    @Basic
    @Column(name = "MONEY", nullable = true)
    private Integer money;
    @Basic
    @Column(name = "BEGIN_AT", nullable = true)
    private Date beginAt;
    @Basic
    @Column(name = "END_AT", nullable = true)
    private Date endAt;
    @Basic
    @Column(name = "CREATE_TIME", nullable = true)
    private Date createTime;
    @Basic
    @Column(name = "CREATE_UP", nullable = true)
    private Integer createUp;
    @Basic
    @Column(name = "UPDATE_DATE", nullable = true)
    private Date updateDate;
    @Basic
    @Column(name = "UPDATE_UP", nullable = true)
    private Integer updateUp;
    @Basic
    @Column(name = "IP", length = 24)
    private String ip;
    @Basic
    @Column(name = "DEL", nullable = true)
    private Integer del;
    @Basic
    @Column(name = "IPARAM1", length = 50)
    private String iparam1;
    @Basic
    @Column(name = "IPARAM2",  length = 50)
    private String iparam2;
    @Basic
    @Column(name = "IPARAM3",  length = 50)
    private String iparam3;
    @Basic
    @Column(name = "VPARAM1", length = 255)
    private String vparam1;
    @Basic
    @Column(name = "VPARAM2",  length = 255)
    private String vparam2;
    @Basic
    @Column(name = "VPARAM3",  length = 255)
    private String vparam3;
}
