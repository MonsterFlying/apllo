package com.gofobao.framework.financial.biz;

import com.gofobao.framework.financial.entity.NewEve;

public interface JixinAssetBiz {

    /**
     * 插入资金变动记录
     * @param newEve
     * @return
     */
    boolean record(NewEve newEve) ;
}
