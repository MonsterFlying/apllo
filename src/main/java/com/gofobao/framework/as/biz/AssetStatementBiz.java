package com.gofobao.framework.as.biz;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 资金核对类
 *
 * @author Max
 */
public interface AssetStatementBiz {

    /**
     * 核对账目
     *
     * @param date 核对账目时间
     * @return
     */
    boolean checkUpAccount(Date date);

    /**
     * 查询全部即信流水
     * @param date
     * @return
     */
    boolean checkUpAccountForAll();
}
