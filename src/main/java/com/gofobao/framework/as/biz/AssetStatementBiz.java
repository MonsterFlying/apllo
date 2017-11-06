package com.gofobao.framework.as.biz;

/**
 * 资金核对类
 *
 * @author Max
 */
public interface AssetStatementBiz {

    /**
     * 核对账目
     *
     * @return
     */
    boolean checkUpAccount();

    /**
     * 查询全部即信流水
     *
     * @return
     */
    boolean checkUpAccountForAll();
}
