package com.gofobao.framework.financial.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 本地资金记录
 */
@Data
public class LocalRecord implements Serializable{
    @Excel(name = "用户昵称", height = 20, width = 30)
    private String userName ;

    @Excel(name = "本地交易流水", height = 20, width = 20)
    private String seqNo ;

    @Excel(name = "电子账户", height = 20, width = 20)
    private String accountId ;

    @Excel(name = "交易名称", height = 20, width = 12)
    private String tranName ;

    @Excel(name = "交易编码", height = 20, width = 5)
    private String tranNo ;

    @Excel(name = "交易金额", height = 20, width = 13)
    private String opMoney ;

    @Excel(name = "变动类型", height = 20, width = 30)
    private String txFlag ;

    @Excel(name = "创建时间", height = 20, width = 30, format="yyyy-MM-dd HH:mm:ss")
    private Date createDate ;
}
