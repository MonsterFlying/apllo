package com.gofobao.framework.system.controller.finance;

import com.gofobao.framework.helper.project.SecurityHelper;
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
import org.springframework.web.bind.annotation.*;

import java.security.Security;

/**
 * Created by admin on 2017/6/14.
 */
@RestController
@Api(description = "首页")
public class FinanceIndexController {

    @Autowired
    private StatisticBiz statisticBiz;

    @Autowired
    private BannerBiz bannerBiz;

    @Autowired
    private HomeBiz homeBiz;

    @GetMapping("/index/finance/v2/statistic")
    public ResponseEntity<VoViewIndexStatisticsWarpRes> statistic() {
        return statisticBiz.query();
    }

    @GetMapping("/index/finance/v2/banner/list")
    public ResponseEntity<VoIndexResp> index() {
        return bannerBiz.index("mobile");
    }

    @GetMapping("/index/finance/v2/product/banner/list")
    public ResponseEntity<VoIndexResp> productIndex() {
        return bannerBiz.index("product");
    }

    @ApiOperation("首页")
    @GetMapping("/pub/finance/index/v2/home")
    public ResponseEntity<VoFinanceIndexResp> home() {
        return homeBiz.financeHome() ;
    }


}
