package com.gofobao.framework.asset.service;


import com.gofobao.framework.asset.entity.Asset;

import java.util.List;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface AssetService {

    /**
     * 根据id产寻资产
     * @param id
     * @return
     */
    Asset findById(Long id);

}
