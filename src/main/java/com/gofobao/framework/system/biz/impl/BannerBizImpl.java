package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.system.biz.BannerBiz;
import com.gofobao.framework.system.service.BannerService;
import com.gofobao.framework.system.vo.response.IndexBanner;
import com.gofobao.framework.system.vo.response.VoViewIndexBannerWarpRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by admin on 2017/6/14.
 */
@Service
public class BannerBizImpl implements BannerBiz {

    @Autowired
    private BannerService bannerService;

    @Override
    public ResponseEntity<VoViewIndexBannerWarpRes> index() {
        VoViewIndexBannerWarpRes warpRes = VoBaseResp.ok("", VoViewIndexBannerWarpRes.class);
        try {
            List<IndexBanner> bannerList = bannerService.index();
            warpRes.setBannerList(bannerList);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewIndexBannerWarpRes.class));
        }
    }
}
