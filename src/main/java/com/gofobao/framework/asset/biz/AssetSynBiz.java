package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.entity.Asset;

/**
 * 资金同步
 */
public interface AssetSynBiz {

    /**
     * 资金同步并且返回资金
     * @param userId
     * @return
     */
    Asset doAssetSyn(Long userId) ;
}
