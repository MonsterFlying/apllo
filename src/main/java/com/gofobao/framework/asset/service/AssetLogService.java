package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.vo.repsonse.VoViewAssetLogRes;
import com.gofobao.framework.asset.vo.request.VoAssetLog;

import java.util.List;

/**
 * Created by admin on 2017/5/22.
 */
public interface AssetLogService {

    /**
     * 资金流水
     * @param voAssetLog
     * @return
     */

    List<VoViewAssetLogRes> assetLogList(VoAssetLog voAssetLog);

    boolean insert(AssetLog assetLog);

    boolean updateById(AssetLog assetLog);
}
