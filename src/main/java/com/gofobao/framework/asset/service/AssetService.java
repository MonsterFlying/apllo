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

    boolean insertById(Asset asset);
    boolean update(Asset asset);
}
