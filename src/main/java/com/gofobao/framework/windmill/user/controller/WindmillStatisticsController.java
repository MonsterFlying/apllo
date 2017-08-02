package com.gofobao.framework.windmill.user.controller;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by admin on 2017/8/1.
 */
@RestController
@RequestMapping("/pub/windmill")
@Slf4j
@ApiModel(description = "风车理财用户注册")
public class WindmillStatisticsController {


    @ApiOperation("每日平台数据统计")
    @GetMapping("statistics/show")
    public String statistics(HttpServletRequest request) {
        return "";
    }


    @ApiOperation("平台公告查询接口")
    @GetMapping("/notice/info/list")
    public String string() {

        return "";
    }


}
