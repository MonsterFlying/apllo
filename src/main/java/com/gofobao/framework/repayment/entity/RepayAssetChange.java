package com.gofobao.framework.repayment.entity;

import com.gofobao.framework.collection.entity.BorrowCollection;
import lombok.Data;

@Data
public class RepayAssetChange {
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
     * 用户收到逾期费用
     */
    private long overdueFee ;

    /**
     * 利息管理费费
     */
    private long interestFee ;

    /**
     * 平台收到逾期费
     */
    private long platformOverdueFee ;

    /**
     * 回款记录
     */
    private BorrowCollection borrowCollection;
}
