package com.gofobao.framework.financial.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Table(name = "gfb_eve")
@Entity
@DynamicUpdate
@DynamicInsert
@Data
public class Eve {
    @Id
    @GeneratedValue
    private Long id  ;
    private String acqcode  ;
    private String seqno  ;
    private String sendt  ;
    private String cardnbr  ;
    private String amount  ;
    private String crflag  ;
    private String msgtype  ;
    private String proccode  ;
    private String mertype  ;
    private String term  ;
    private String retseqno  ;
    private String conmode  ;
    private String autresp  ;
    private String forcode  ;
    private String clrdate  ;
    private String oldseqno  ;
    private String openbrno  ;
    private String tranbrno  ;
    private String ervind  ;
    private String transtype  ;
    private String queryDate ;
    private Date createAt  ;
}
