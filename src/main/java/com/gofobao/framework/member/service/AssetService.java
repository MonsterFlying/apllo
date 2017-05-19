package com.gofobao.framework.member.service;

import com.gofobao.framework.member.entity.Asset;

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
