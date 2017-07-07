package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.asset.biz.AssetChangeLogBiz;
import com.gofobao.framework.asset.service.AssetChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/7/7 0007.
 */
@Component
public class AssetChangeLogBizImpl implements AssetChangeLogBiz {

    @Autowired
    AssetChangeLogService assetChangeLogService ;
}
