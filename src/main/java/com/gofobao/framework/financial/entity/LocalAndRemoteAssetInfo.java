package com.gofobao.framework.financial.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.util.Date;

@Data
public class LocalAndRemoteAssetInfo {
    @Excel(name = "平台用户编号", height = 10, width = 30)
    private String userId ;

    @Excel(name = "平台最新金额", height = 10, width = 30)
    private String localMoney ;

    @Excel(name = "即信最新金额", height = 10, width = 30)
    private String remoteMoney ;

    @Excel(name = "平台最新更新时间", height = 10, width = 30, format="yyyy-MM-dd HH:mm:ss")
    private Date localUpdateDatetime ;

    @Excel(name = "即信最新更新时间", height = 10, width = 30)
    private String remoteUpdateDatetime ;

    @Excel(name = "电子账户", height = 10, width = 30)
    private String accountId ;

    @Excel(name = "手机", height = 10, width = 30)
    private String phone ;

    @Excel(name = "真实姓名", height = 10, width = 30)
    private String realname ;

    @Excel(name = "即信与本地相差金额", height = 10, width = 30)
    private String diffMoney ;
}
