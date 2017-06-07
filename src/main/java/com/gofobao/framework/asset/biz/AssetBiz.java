package com.gofobao.framework.asset.biz;

import com.gofobao.framework.asset.vo.request.VoAssetLog;
import com.gofobao.framework.asset.vo.response.VoUserAssetInfoResp;
import com.gofobao.framework.asset.vo.response.VoViewAssetLogWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface AssetBiz {

    /**
     * 获取用户资产详情
     * @param userId
     * @return
     */
    ResponseEntity<VoUserAssetInfoResp> userAssetInfo(Long userId);


    /**
     * 用户账户流水
     * @return
     */
   ResponseEntity<VoViewAssetLogWarpRes> assetLogResList(VoAssetLog voAssetLog) ;

}
