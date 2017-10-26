package com.gofobao.framework.as.bix;

import com.gofobao.framework.as.bix.impl.RechargeStatementBizImpl;

import java.util.Date;

/**
 * 充值对账
 *
 * @author Administrator
 */
public interface RechargeStatementBiz {

    /**
     * 离线充值记录匹配
     *
     * @param userId 用户编号
     * @param date 对账时间
     * @param rechargeType 充值类型
     * @return
     */
    boolean offlineStatement(Long userId, Date date, RechargeStatementBizImpl.RechargeType rechargeType) throws Exception;


    /**
     * 实时充值记录匹配
     * 注意:
     *  设置force 为true时, 必须保证用户在前30分钟内没有相应的提现操作
     * @param userId 用户编号
     * @param date 对账时间
     * @param rechargeType 充值类型
     * @param force 是否前置对账
     * @return
     */
    boolean onlineStatement(Long userId, Date date, RechargeStatementBizImpl.RechargeType rechargeType, boolean force) throws Exception;
}
