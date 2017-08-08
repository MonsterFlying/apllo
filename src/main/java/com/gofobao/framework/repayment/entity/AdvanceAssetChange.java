package com.gofobao.framework.repayment.entity;

import lombok.Data;

@Data
public class AdvanceAssetChange {
    private long userId ;
    /**
     *  还款本金本金
     */
    private long principal ;

    /**
     * 还款利息利息
     */
    private long interest ;

    /**
     * 逾期管理费
     */
    private long overdueFee ;
}
