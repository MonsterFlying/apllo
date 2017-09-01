package com.gofobao.framework.asset.biz;

/**
 * 资金同步
 */
public interface AssetSynBiz {

    /**
     * 资金同步并且返回资金
     * @param userId
     * @return
     */
    boolean doAssetSyn(Long userId) throws Exception;
}
