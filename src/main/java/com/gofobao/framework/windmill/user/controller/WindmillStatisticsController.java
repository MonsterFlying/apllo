package com.gofobao.framework.windmill.user.controller;

import com.gofobao.framework.windmill.borrow.biz.impl.WindmillStatisticsBizImpl;
import com.gofobao.framework.windmill.borrow.vo.response.ByDayStatistics;
import com.gofobao.framework.windmill.borrow.vo.response.UserAccountStatistics;
import com.gofobao.framework.windmill.user.vo.respones.VoNoticesRes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Autowired
    private WindmillStatisticsBizImpl statisticsBiz;

    @ApiOperation("每日平台数据统计")
    @RequestMapping("statistics/show")
    public ByDayStatistics statistics(HttpServletRequest request) {
        return statisticsBiz.byDayStatistics(request);
    }

    @ApiOperation("查询用户账户信息")
    @RequestMapping("/user/basics/info")
    public UserAccountStatistics userInfo(HttpServletRequest request) {
        return statisticsBiz.userStatistics(request);
    }
    
    @ApiOperation("平台公告查询接口")
    @RequestMapping("/notice/info/list")
    public VoNoticesRes string(HttpServletRequest request) {
        return statisticsBiz.noticesList(request);
    }


}
