package com.gofobao.framework.repayment.entity;

import com.gofobao.framework.collection.entity.BorrowCollection;
import lombok.Data;

@Data
public class RepayAssetChange {
    private long userId = 0;
    /**
     *  还款本金本金
     */
    private long principal = 0;

    /**
     * 还款利息利息
     */
    private long interest = 0;

    /**
     * 用户收到逾期费用
     */
    private long overdueFee = 0;

    /**
     * 利息管理费费
     */
    private long interestFee = 0;

    /**
     * 平台收到逾期费
     */
    private long platformOverdueFee = 0;

    /**
     * 回款记录
     */
    private BorrowCollection borrowCollection;
}
