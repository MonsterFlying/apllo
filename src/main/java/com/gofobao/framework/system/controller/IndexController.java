package com.gofobao.framework.system.controller;

import com.gofobao.framework.system.biz.BannerBiz;
import com.gofobao.framework.system.biz.HomeBiz;
import com.gofobao.framework.system.biz.StatisticBiz;
import com.gofobao.framework.system.vo.response.VoIndexResp;
import com.gofobao.framework.system.vo.response.VoViewIndexStatisticsWarpRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by admin on 2017/6/14.
 */
@RestController
@Api(description = "首页")
public class IndexController {

    @Autowired
    private StatisticBiz statisticBiz;

    @Autowired
    private BannerBiz bannerBiz;

    @Autowired
    private HomeBiz homeBiz;

    @GetMapping("/index/v2/statistic")
    public ResponseEntity<VoViewIndexStatisticsWarpRes> statistic() {
        return statisticBiz.query();
    }

    @GetMapping("/index/v2/banner/list")
    public ResponseEntity<VoIndexResp> index() {
        return bannerBiz.index("mobile");
    }


    @ApiOperation("首页")
    @GetMapping("/pub/index/v2/home")
    public ResponseEntity<VoIndexResp> home() {
        return homeBiz.home() ;
    }


}
