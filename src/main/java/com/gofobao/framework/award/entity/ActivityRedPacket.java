package com.gofobao.framework.award.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;


/**
 * Created by admin on 2017/5/31.
 */
@Entity(name = "ActivityRedPacket")
@Table(name = "gfb_activity_red_packet")
@Data
public class ActivityRedPacket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer activityId;
    private String activityName;
    private Integer status;

    private Long userId;

    private String userName;

    private Integer money;

    private Date beginAt;

    private Date endAt;

    private Date createTime;

    private Integer createUp;

    private Date updateDate;

    private Integer updateUp;

    private String ip;

    private Integer del;

    private String iparam1;

    private String iparam2;

    private String iparam3;

    private String vparam1;

    private String vparam2;

    private String vparam3;
}
