package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.repository.AssetRepository;
import com.gofobao.framework.asset.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Zeke on 2017/5/19.
 */
@Service
public class AssetServiceImpl implements AssetService{

    @Autowired
    private AssetRepository assetRepository;

    /**
     * 根据id产寻资产
     * @param id
     * @return
     */
    public Asset findById(Long id){
        return assetRepository.findOne(id);
    }
}
