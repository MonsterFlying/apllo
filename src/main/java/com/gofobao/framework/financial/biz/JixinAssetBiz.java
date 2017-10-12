package com.gofobao.framework.financial.biz;

public interface JixinAssetBiz {

    /**
     * 插入资金变动记录
     * @param cardnbr
     * @param crflag
     * @param amount
     * @return
     */
    boolean record(String cardnbr,String crflag,String amount) ;
}
