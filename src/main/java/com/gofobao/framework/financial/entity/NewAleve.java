package com.gofobao.framework.financial.entity;


import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "gfb_new_Aleve")
@DynamicInsert
@DynamicUpdate
@Data
public class NewAleve {

    @Id
    @GeneratedValue
    private Long id ;
    /**
     * 银行号
     */
    private String  bank ;

    /**
     * 电子账号
     */
    private String  cardnbr ;

    /**
     * 交易金额
     */
    private String  amount ;

    /**
     * 货币代码
     */
    private String  curNum ;

    /**
     * 交易金额符号
     */
    private String  crflag ;

    /**
     * 入帐日期
     */
    private String  valdate ;

    /**
     * 交易日期
     */
    private String  inpdate ;

    /**
     * 自然日期
     */
    private String  reldate ;

    /**
     * 交易时间
     */
    private String  inptime ;

    /**
     * 交易流水号
     */
    private String  tranno ;

    /**
     * 关联交易流水号
     */
    private String  oriTranno ;

    /**
     * 交易类型
     */
    private String  transtype ;

    /**
     * 交易描述
     */
    private String  desline ;

    /**
     * 交易后余额
     */
    private String  currBal ;

    /**
     * 对手交易帐号
     */
    private String  forcardnbr ;

    /**
     * 冲正、撤销标志
     */
    private String  revind ;

    /**
     * 交易标识
     */
    private String  accchg ;

    /**
     * 系统跟踪号
     */
    private String  seqno ;

    /**
     * 原交易流水号
     */
    private String  oriNum ;

    /**
     * 保留域
     */
    private String  resv  ;

    private String queryTime ;

}
