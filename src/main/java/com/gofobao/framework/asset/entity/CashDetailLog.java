package com.gofobao.framework.asset.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by Administrator on 2017/6/13 0013.
 */
@Data
@Entity
@Table(name = "gfb_cash_detail_log")
public class CashDetailLog {
    @Id
    @GeneratedValue
    private Long id ;
    private String seqNo ;
    private Long userId ;
    private String thirdAccountId ;
    private String cardNo ;
    private String bankName ;
    private String companyBankNo ;
    private Long money ;
    private Long fee ;
    private Date verifyTime ;
    private Long verifyUserId ;
    private String verifyRemark ;
    private Integer state ;
    private Date createTime ;
    private Date callbackTime ;
    private Date cancelTime ;
    private String ip ;
    private Integer cashType ;
}
