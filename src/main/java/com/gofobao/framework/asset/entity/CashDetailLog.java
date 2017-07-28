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
 * Created by Administrator on 2017/6/13 0013.
 */
@Data
@Entity
@Table(name = "gfb_cash_detail_log")
@DynamicInsert
@DynamicUpdate
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
    private String querySeqNo ;
    private String queryCallbackTime ;
}
