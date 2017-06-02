package com.gofobao.framework.asset.service;


import com.gofobao.framework.asset.entity.Asset;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface AssetService {

    /**
     * 根据id产寻资产
     * @param id
     * @return
     */
    Asset findByUserId(Long id);

    Asset findByUserIdLock(Long id);

    Asset save(Asset asset);

    Asset updateById(Asset asset);
}
