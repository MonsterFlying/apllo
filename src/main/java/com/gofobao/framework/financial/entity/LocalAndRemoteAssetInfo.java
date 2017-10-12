package com.gofobao.framework.financial.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.util.Date;

@Data
public class LocalAndRemoteAssetInfo {
    @Excel(name = "平台最新金额", height = 10, width = 30)
    private String localMoney ;

    @Excel(name = "即信最新金额", height = 10, width = 30)
    private String remoteMoney ;

    @Excel(name = "平台最新更新时间", height = 10, width = 30, format="yyyy-MM-dd HH:mm:ss")
    private Date localUpdateDatetime ;

    @Excel(name = "即信最新更新时间", height = 10, width = 30, format="yyyy-MM-dd HH:mm:ss")
    private Date remoteUpdateDatetime ;

    @Excel(name = "用户昵称", height = 10, width = 30)
    private String username ;

    @Excel(name = "手机", height = 10, width = 30)
    private String phone ;

    @Excel(name = "真实姓名", height = 10, width = 30)
    private String realname ;
}
