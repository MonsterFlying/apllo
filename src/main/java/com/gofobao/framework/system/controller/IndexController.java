package com.gofobao.framework.system.controller;

import com.gofobao.framework.system.biz.BannerBiz;
import com.gofobao.framework.system.biz.StatisticBiz;
import com.gofobao.framework.system.vo.response.VoViewIndexBannerWarpRes;
import com.gofobao.framework.system.vo.response.VoViewIndexStatisticsWarpRes;
import io.swagger.annotations.ApiModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by admin on 2017/6/14.
 */
@RestController
@ApiModel("首页")
@RequestMapping("/index")
public class IndexController {

    @Autowired
    private StatisticBiz statisticBiz;
    @Autowired
    private BannerBiz bannerBiz;

    @GetMapping("/v2/statistic")
    public ResponseEntity<VoViewIndexStatisticsWarpRes> statistic() {
        return statisticBiz.query();
    }

    @GetMapping("/v2/banner/list")
    public  ResponseEntity<VoViewIndexBannerWarpRes>index(){
        return bannerBiz.index();
    }



}
