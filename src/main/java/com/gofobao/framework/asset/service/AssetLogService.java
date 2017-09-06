package com.gofobao.framework.asset.service;

import com.gofobao.framework.asset.entity.AssetLog;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.vo.request.VoAssetLogReq;
import com.gofobao.framework.asset.vo.response.VoViewAssetLogRes;
import com.gofobao.framework.asset.vo.response.pc.AssetLogs;

import java.util.List;

/**
 * Created by admin on 2017/5/22.
 */
public interface AssetLogService {

    /**
     * 资金流水
     * @param voAssetLogReq
     * @return
     */

    List<VoViewAssetLogRes> assetLogList(VoAssetLogReq voAssetLogReq);

    void insert(AssetLog assetLog);

    void updateById(AssetLog assetLog);

    /**
     * pc: 资金流水
     * @param voAssetLogReq
     * @return
     */
    List<AssetLogs> pcAssetLogs(VoAssetLogReq voAssetLogReq);


    /**
     * pc：资金流水导出到excel
     * @param voAssetLogReq
     * @return
     */
    List<NewAssetLog> pcToExcel(VoAssetLogReq voAssetLogReq);
}
