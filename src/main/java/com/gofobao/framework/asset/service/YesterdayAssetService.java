package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.YesterdayAsset;

/**
 * Created by Zeke on 2017/5/22.
 */
public interface YesterdayAssetService {
    YesterdayAsset findByUserId(Long userId);
    boolean insert(YesterdayAsset yesterdayAsset);
    boolean update(YesterdayAsset yesterdayAsset);

}
