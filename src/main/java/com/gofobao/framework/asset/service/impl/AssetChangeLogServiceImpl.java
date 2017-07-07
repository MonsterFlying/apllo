package com.gofobao.framework.asset.service.impl;

import com.gofobao.framework.asset.repository.AssetChangeLogRepository;
import com.gofobao.framework.asset.service.AssetChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2017/7/7 0007.
 */
@Service
public class AssetChangeLogServiceImpl implements AssetChangeLogService {
    @Autowired
    AssetChangeLogRepository assetChangeLogRepository ;
}
