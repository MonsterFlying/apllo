package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.YesterdayAsset;
import com.gofobao.framework.asset.repository.YesterdayAssetRepository;
import com.gofobao.framework.asset.service.YesterdayAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

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

    public void insert(YesterdayAsset yesterdayAsset){

         yesterdayAssetRepository.save(yesterdayAsset);
    }

    public void update(YesterdayAsset yesterdayAsset){
       yesterdayAssetRepository.save(yesterdayAsset);
    }
}
