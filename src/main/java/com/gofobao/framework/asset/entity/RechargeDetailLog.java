package com.gofobao.framework.asset.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Max on 17/6/7.
 */
@Entity
@Table(name = "gfb_recharge_detail_log")
@Data
@DynamicInsert
@DynamicUpdate
public class RechargeDetailLog {
    @Id
    @GeneratedValue
    private Long id ;
    private Long userId;
    private String seqNo ;
    private Date createTime ;
    private Date callbackTime ;
    private Integer state ;
    private Integer del ;
    private Integer rechargeType ;
    private String cardNo ;
    private String bankName ;
    private Long money ;
    private Integer rechargeSource ;
    private Integer rechargeChannel ;
    private String remark ;
    private String mobile ;
    private Date updateTime ;
    private String ip ;
    private String responseMessage ;
}
