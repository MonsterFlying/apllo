package com.gofobao.framework.financial.entity;


import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "gfb_new_eve")
@DynamicInsert
@DynamicUpdate
@Data
public class NewEve {
    @Id
    @GeneratedValue
    private Long id ;

    /**
     * 发送方标识码
     */
    private String forcode;

    /**
     * 系统跟踪号
     */
    private String seqno;

    /**
     * 交易传输时间 MMDDHHMMSS
     */
    private String cendt;

    /**
     * 主账号
     */
    private String cardnbr;

    /**
     * 交易金额
     */
    private String amount;

    /**
     * 交易金额符号
     */
    private String crflag;

    /**
     * 消息类型
     */
    private String msgtype;

    /**
     * 交易类型码
     */
    private String proccode;

    /**
     * 订单号
     */
    private String orderno;

    /**
     * 内部交易流水号
     */
    private String tranno;

    /**
     * 内部保留域
     */
    private String reserved;

    /**
     * 冲正、撤销标志
     */
    private String ervind;

    /**
     * 主机交易类型
     */
    private String transtype;

    /**
     * 对账日期
     */
    private String queryTime;
}
