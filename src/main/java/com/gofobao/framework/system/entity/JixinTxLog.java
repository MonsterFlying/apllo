package com.gofobao.framework.system.entity;

import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by admin on 2017/5/31.
 */
@Entity
@Table(name = "gfb_jixin_tx_log")
@Data
@DynamicInsert
@DynamicUpdate
public class JixinTxLog {
    @Id
    @GeneratedValue
    private Long id;

    private String seqNo  ;
    private String body  ;
    private Date createAt  ;
    private String txType  ;
    private String txTypeDesc  ;
    private Integer type  ;
}
