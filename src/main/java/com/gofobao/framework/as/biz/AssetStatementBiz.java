package com.gofobao.framework.as.biz;

import java.util.Date;

/**
 * 资金核对类
 * @author Max
 */
public interface AssetStatementBiz {

    /**
     * 核对账目
     * @param date 核对账目时间
     * @return
     */
    boolean checkUpAccount(Date date) ;
}
