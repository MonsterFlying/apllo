package com.gofobao.framework.award.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by admin on 2017/5/31.
 */
@Entity(name = "ActivityRedPacketLog")
@Table(name = "gfb_activity_red_packet_log")
@Data
public class ActivityRedPacketLog {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Basic
    @Column(name = "RED_PACKET_ID", nullable = true)
    private Integer redPacketId;
    @Basic
    @Column(name = "USER_ID", nullable = true)
    private Integer userId;
    @Basic
    @Column(name = "USER_NAME", nullable = true, length = 20)
    private String userName;
    @Basic
    @Column(name = "TERMNAL", nullable = true)
    private Integer termnal;
    @Basic
    @Column(name = "CREATE_TIME", nullable = true)
    private Date createTime;
    @Basic
    @Column(name = "CREATE_UP", nullable = true)
    private Integer createUp;
    @Basic
    @Column(name = "UPDATE_TIME", nullable = true)
    private Date updateTime;
    @Basic
    @Column(name = "UPDARE_UP", nullable = true)
    private Integer updareUp;
    @Basic
    @Column(name = "DEL", nullable = true)
    private Integer del;
    @Basic
    @Column(name = "IPARAM1", nullable = true)
    private Integer iparam1;
    @Basic
    @Column(name = "IPARAM2", nullable = true)
    private Integer iparam2;
    @Basic
    @Column(name = "VPARAM1", nullable = true, length = 255)
    private String vparam1;
    @Basic
    @Column(name = "VPARAM2", nullable = true, length = 255)
    private String vparam2;


    public ActivityRedPacketLog(){}

}
