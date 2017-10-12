package com.gofobao.framework.financial.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 本地资金记录
 */
@Data
public class RemoteRecord implements Serializable{
    @Excel(name = "用户昵称", height = 10, width = 30)
    private String userName ;

    @Excel(name = "交易流水", height = 10, width = 30)
    private String seqNo ;

    @Excel(name = "电子账户", height = 10, width = 30)
    private String accountId ;

    @Excel(name = "交易名称", height = 10, width = 15)
    private String tranName ;

    @Excel(name = "交易编码", height = 10, width = 10)
    private String tranNo ;

    @Excel(name = "交易金额", height = 10, width = 25)
    private String opMoney ;

    @Excel(name = "变动类型", height = 10, width = 10)
    private String txFlag ;

    @Excel(name = "手机号", height = 10, width = 30)
    private String phone ;

    @Excel(name = "冲正、撤销标志", height = 10, width = 30)
    private String ervind ;

    @Excel(name = "请求时间", height = 10, width = 30)
    private String cendt ;
}
