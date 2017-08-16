package com.gofobao.framework.award.controller;

import com.gofobao.framework.award.biz.VirtualBiz;
import com.gofobao.framework.award.vo.response.VoViewAwardStatisticsWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by admin on 2017/6/13.
 */
@Api(description = "我的奖励")
@RestController
public class AwardController {

    @Autowired
    private VirtualBiz virtualBiz;

    @ApiOperation("奖励统计")
    @GetMapping("/award/v2/statistics")
    public ResponseEntity<VoViewAwardStatisticsWarpRes> statistics(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
       return virtualBiz.statistics(userId);
    }

}
