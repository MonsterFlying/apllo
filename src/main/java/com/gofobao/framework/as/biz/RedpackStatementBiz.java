package com.gofobao.framework.as.biz;

import java.util.Date;

/**
 * 红包投资
 */
public interface RedpackStatementBiz {
    /**
     * 离线红包匹配
     *
     * @param userId 用户编号
     * @param date 对账时间
     * @return
     */
    boolean offlineStatement(Long userId, Date date) throws Exception;
}
