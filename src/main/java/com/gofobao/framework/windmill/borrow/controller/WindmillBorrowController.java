package com.gofobao.framework.windmill.borrow.controller;

import com.gofobao.framework.windmill.borrow.biz.WindmillBorrowBiz;
import com.gofobao.framework.windmill.borrow.biz.WindmillStatisticsBiz;
import com.gofobao.framework.windmill.borrow.vo.response.*;
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
@ApiModel(description = "风车理财标的接口")
public class WindmillBorrowController {

    @Autowired
    private WindmillBorrowBiz windmillBorrowBiz;

    @Autowired
    private WindmillStatisticsBiz windmillStatisticsBiz;

    @ApiOperation("标列表")
    @GetMapping("/invest/list")
    public InvestListRes list(HttpServletRequest request) {
        return windmillBorrowBiz.list(request);
    }

    @ApiOperation("标的投标记录")
    @GetMapping("/borrow/info")
    public BorrowTenderList info(HttpServletRequest request) {
        return windmillBorrowBiz.tenderList(request);
    }

    @ApiOperation("查询某天投资情况")
    @GetMapping("/invest/byTime/list")
    public BySomeDayRes byTime(HttpServletRequest request) {
        return windmillBorrowBiz.bySomeDayTenders(request);
    }

    @ApiOperation("查询每日的汇总数据")
    @GetMapping("/invest/bySomeday/list")
    public ByDayStatistics bySomeday(HttpServletRequest request) {
        return windmillStatisticsBiz.byDayStatistics(request);
    }

    @ApiOperation("用户投资记录查询接口")
    @PostMapping("user/invest/list")
    public UserAccountStatistics userInvestList(HttpServletRequest request) {
        return windmillStatisticsBiz.userStatistics(request);
    }


}
