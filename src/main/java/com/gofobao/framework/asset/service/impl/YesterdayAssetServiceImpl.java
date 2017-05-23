package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.YesterdayAsset;
import com.gofobao.framework.asset.repository.YesterdayAssetRepository;
import com.gofobao.framework.asset.service.YesterdayAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.validation.constraints.Null;

/**
 * Created by Zeke on 2017/5/22.
 */
@Service
public class YesterdayAssetServiceImpl implements YesterdayAssetService{
    @Autowired
    private YesterdayAssetRepository yesterdayAssetRepository;

    public YesterdayAsset findByUserId(Long userId){
        return yesterdayAssetRepository.findOne(userId);
    }

    public boolean insert(YesterdayAsset yesterdayAsset){
        if (ObjectUtils.isEmpty(yesterdayAsset)){
           return false;
        }
        yesterdayAsset.setUserId(null);
        return !ObjectUtils.isEmpty(yesterdayAssetRepository.save(yesterdayAsset));
    }

    public boolean update(YesterdayAsset yesterdayAsset){
        if (ObjectUtils.isEmpty(yesterdayAsset) || ObjectUtils.isEmpty(yesterdayAsset.getUserId())){
            return false;
        }
        return !ObjectUtils.isEmpty(yesterdayAssetRepository.save(yesterdayAsset));
    }
}
