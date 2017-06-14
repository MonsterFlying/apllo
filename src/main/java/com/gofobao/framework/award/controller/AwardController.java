package com.gofobao.framework.award.controller;

import com.gofobao.framework.award.biz.VirtualBiz;
import com.gofobao.framework.award.vo.response.VoViewAwardStatisticsWarpRes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by admin on 2017/6/13.
 */
@ApiModel("我的奖励")
@RequestMapping("award")
@RestController
public class AwardController {

    @Autowired
    private VirtualBiz virtualBiz;

    @ApiOperation("奖励统计")
    @GetMapping("/v2/statistics")
    public ResponseEntity<VoViewAwardStatisticsWarpRes> statistics(){
        Long userId=901L;
       return virtualBiz.statistics(userId);
    }

}
