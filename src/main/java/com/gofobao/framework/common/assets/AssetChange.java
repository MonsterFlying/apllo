package com.gofobao.framework.common.assets;


import lombok.Data;

@Data
public class AssetChange {
    /**
     * 操作用户Id
     */
    private Long userId ;

    /**
     * 对手用户Id
     */
    private Long forUserId ;

    /**
     * 操作金额
     */
    private long money ;

    /**
     * 本金
     */
    private long principal ;

    /**
     * 利息
     */
    private long interest ;

    /**
     * 记录
     */
    private String remark ;

    /**
     * 本地流水
     */
    private String seqNo;

    /**
     * 资金变动类型
     */
    private AssetChangeTypeEnum type;

    /**
     * 资金变动触发类型
     */
    private Long sourceId ;

    /**
     * 区别为一组操作
     */
    private String groupSeqNo ;
}
