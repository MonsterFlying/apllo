package com.gofobao.framework.as.biz;

/**
 * 资金核对类
 *
 * @author Max
 */
public interface AssetStatementBiz {

    /**
     * 根据昨日变动查询
     *
     * @return
     */
    boolean checkUpAccountForChange();


    /**
     * 查询全部开户账号
     *
     * @return
     */
    boolean checkUpAccountForAll();


    /**
     * 根据活跃用户对账账户
     * @return
     */
    boolean checkUpAccountForActiveState() ;
}
