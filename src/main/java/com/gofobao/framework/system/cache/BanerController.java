package com.gofobao.framework.system.cache;

import com.gofobao.framework.borrow.vo.request.VoDoAgainVerifyReq;
import com.gofobao.framework.system.biz.BannerBiz;
import com.gofobao.framework.system.biz.HomeBiz;
import com.gofobao.framework.system.biz.StatisticBiz;
import com.gofobao.framework.system.vo.response.BanerCache;
import com.gofobao.framework.system.vo.response.VoFinanceIndexResp;
import com.gofobao.framework.system.vo.response.VoIndexResp;
import com.gofobao.framework.system.vo.response.VoViewIndexStatisticsWarpRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by admin on 2017/6/14.
 */
@RestController
@Api(description = "首页")
public class BanerController {

    @Autowired
    private StatisticBiz statisticBiz;

    @Autowired
    private BannerBiz bannerBiz;

    @Autowired
    private HomeBiz homeBiz;

    /**
     * 清除banner缓存
     */
    @PostMapping("/index/banner/cache/clear")
    public ResponseEntity<BanerCache> clearCache(VoDoAgainVerifyReq voDoAgainVerifyReq) {
        return bannerBiz.clear(voDoAgainVerifyReq);
    }


}
