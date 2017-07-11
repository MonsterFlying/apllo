package com.gofobao.framework.common.capital;

import lombok.Data;

/**
 * 资金变动类
 * Created by Max on 17/3/10.
 */
@Data
public class CapitalChangeEntity {
    /**
     * 用户Id
     */
    private long userId;

    /**
     * userID
     */
    private long toUserId;

    /**
     * 操作总金额
     */
    private long money;

    /**
     * 本金
     */
    private long principal;

    /**
     * 资产操作
     */
    private String asset;

    /**
     * 利息
     */
    private int interest;

    /**
     * 变动类型
     */
    private CapitalChangeEnum type;

    /**
     * 备注
     */
    private String remark;


}
