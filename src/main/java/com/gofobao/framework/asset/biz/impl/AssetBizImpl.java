package com.gofobao.framework.asset.biz.impl;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetLogService;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.asset.vo.request.VoAssetLog;
import com.gofobao.framework.asset.vo.response.VoUserAssetInfoResp;
import com.gofobao.framework.asset.vo.response.VoViewAssetLogRes;
import com.gofobao.framework.asset.vo.response.VoViewAssetLogWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.entity.UserCache;
import com.gofobao.framework.member.service.UserCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * Created by Zeke on 2017/5/19.
 */
@Service
public class AssetBizImpl implements AssetBiz {

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetLogService assetLogService;

    @Autowired
    private UserCacheService userCacheService;

    /**
     * 获取用户资产详情
     *
     * @param userId
     * @return
     */
    public ResponseEntity<VoUserAssetInfoResp> userAssetInfo(Long userId) {

        Asset asset = assetService.findByUserId(userId); //查询会员资产信息
        if (ObjectUtils.isEmpty(asset)) {
            return null;
        }


        UserCache userCache = userCacheService.findById(userId);  //查询会员缓存信息
        if (ObjectUtils.isEmpty(userCache)) {
            return null;
        }

        Integer useMoney = asset.getUseMoney();
        Integer waitCollectionPrincipal = userCache.getWaitCollectionPrincipal();
        Integer payment = asset.getPayment();
        int netWorthQuota = new Double((useMoney + waitCollectionPrincipal) * 0.8 - payment).intValue();//计算净值额度

        VoUserAssetInfoResp voUserAssetInfoResp = new VoUserAssetInfoResp();
        voUserAssetInfoResp.setUseMoney(useMoney);
        voUserAssetInfoResp.setNoUseMoney(asset.getNoUseMoney());
        voUserAssetInfoResp.setPayment(payment);
        voUserAssetInfoResp.setCollection(asset.getCollection());
        voUserAssetInfoResp.setNetWorthQuota(netWorthQuota);
        return ResponseEntity.ok(voUserAssetInfoResp);
    }

    /**
     * 账户流水
     *
     * @return
     */
    @Override
    public ResponseEntity<VoViewAssetLogWarpRes> assetLogResList(VoAssetLog voAssetLog) {
        try {
            List<VoViewAssetLogRes> resList = assetLogService.assetLogList(voAssetLog);
            VoViewAssetLogWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewAssetLogWarpRes.class);
            warpRes.setResList(resList);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.ok("查询成功", VoViewAssetLogWarpRes.class));
        }
    }
}
