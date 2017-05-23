package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.repository.AssetLogRepository;
import com.gofobao.framework.asset.service.AssetLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * Created by Zeke on 2017/5/22.
 */
@Service
public class AssetLogServiceImpl implements AssetLogService{

    @Autowired
    private AssetLogRepository assetLogRepository;

    public boolean insert(AssetLog assetLog){
        if (ObjectUtils.isEmpty(assetLog)){
            return false;
        }
        assetLog.setId(null);
        return !ObjectUtils.isEmpty(assetLogRepository.save(assetLog));
    }

    public boolean update(AssetLog assetLog){
        if (ObjectUtils.isEmpty(assetLog) || ObjectUtils.isEmpty(assetLog.getId())){
            return false;
        }
        return !ObjectUtils.isEmpty(assetLogRepository.save(assetLog));
    }
}
