package com.gofobao.framework.scheduler.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * 状态查询
 * Created by Administrator on 2017/6/30 0030.
 */
@Data
@Entity
@Table(name = "gfb_task_scheduler")
@DynamicInsert
@DynamicUpdate
public class TaskScheduler {
    @Id
    @GeneratedValue
    private Long id;
    private Integer type ;
    private Integer taskNum ;
    private Integer state ;
    private String taskData;
    private Integer del ;
    private Integer doTaskNum ;
    private String doTaskData ;
    private Date createAt;
    private Date updateAt ;
}
