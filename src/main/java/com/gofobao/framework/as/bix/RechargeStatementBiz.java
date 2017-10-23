package com.gofobao.framework.as.bix;

import java.util.Date;

/**
 * 充值对账
 *
 * @author Administrator
 */
public interface RechargeStatementBiz {

    /**
     * 匹配线下充值
     *
     * @param userId 用户编号
     * @param date 对账时间
     * @return
     */
    boolean matchOfflineRecharge(Long userId, Date date);


    /**
     * 匹配线上充值
     *
     * @param userId 用户编号
     * @param date 对账时间
     * @return
     */
    boolean matchOnlineRecharge(Long userId, Date date);
}
