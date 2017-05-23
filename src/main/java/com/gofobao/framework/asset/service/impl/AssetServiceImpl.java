package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.repository.AssetRepository;
import com.gofobao.framework.asset.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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
    public Asset findByUserId(Long id){
        return assetRepository.findOne(id);
    }

    public Asset findByUserIdLock(Long id){
        return assetRepository.findByUserId(id);
    }

    public boolean insert(Asset asset){
        if (ObjectUtils.isEmpty(asset)){
            return false;
        }
        asset.setUserId(null);
        return !ObjectUtils.isEmpty(assetRepository.save(asset));
    }

    public boolean update(Asset asset){
        if (ObjectUtils.isEmpty(asset) || ObjectUtils.isEmpty(asset.getUserId())){
            return false;
        }
        return !ObjectUtils.isEmpty(assetRepository.save(asset));
    }
}
