package com.gofobao.framework.as.service.impl;

import com.gofobao.framework.as.entity.RealtimeAsset;
import com.gofobao.framework.as.repository.RealtimeAssetRepository;
import com.gofobao.framework.as.service.RealtimeAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RealtimeAssetServiceImpl implements RealtimeAssetService {
    @Autowired
    RealtimeAssetRepository realtimeAssetRepository;


    @Override
    public RealtimeAsset save(RealtimeAsset realtimeAsset) {
        return realtimeAssetRepository.save(realtimeAsset);
    }
}
