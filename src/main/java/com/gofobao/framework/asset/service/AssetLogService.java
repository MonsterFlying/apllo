package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.AssetLog;

/**
 * Created by Zeke on 2017/5/22.
 */
public interface AssetLogService {
    boolean insert(AssetLog assetLog);
    boolean update(AssetLog assetLog);
}
