package com.gofobao.framework.repayment.entity;

import com.gofobao.framework.collection.entity.BorrowCollection;
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
     * 逾期滞纳金
     */
    private long overdueFee ;

    /**
     * 回款记录
     */
    private BorrowCollection borrowCollection;
}
