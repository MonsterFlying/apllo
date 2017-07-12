package com.gofobao.framework.common.assets;

import lombok.Data;

/**
 * 资金变动
 * Created by Administrator on 2017/7/7 0007.
 */
@Data
public class AssetsChangeEntity {
    /** 操作用户Id*/
    private long userId ;
    /** 平台类型*/
    private AssetsChangeEnum type ;
    /**  引发资金变动的Id*/
    private long refId;
    /** 对手操作用户Id*/
    private long toUserId;
    /** 本金 */
    private long principal ;
    /**  利息 */
    private int interest;
    /** 操作金额*/
    private long  money ;
    /** 手续费*/
    private long fee ;
    /**  备注*/
    private String remark ;
    /**  需要替换的资金变化权限*/
    private String assetChangeRule ;
}
