package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.repository.NewAssetLogRepository;
import com.gofobao.framework.asset.service.NewAssetLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NewAssetLogServiceImpl implements NewAssetLogService {

    @Autowired
    NewAssetLogRepository newAssetLogRepository ;


    @Override
    public NewAssetLog save(NewAssetLog newAssetLog) {
        return newAssetLogRepository.save(newAssetLog) ;
    }
}
