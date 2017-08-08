package com.gofobao.framework.windmill.borrow.controller;

import com.gofobao.framework.windmill.borrow.biz.WindmillBorrowBiz;
import com.gofobao.framework.windmill.borrow.biz.WindmillStatisticsBiz;
import com.gofobao.framework.windmill.borrow.vo.response.BorrowTenderList;
import com.gofobao.framework.windmill.borrow.vo.response.ByDayStatistics;
import com.gofobao.framework.windmill.borrow.vo.response.BySomeDayRes;
import com.gofobao.framework.windmill.borrow.vo.response.InvestListRes;
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
    @RequestMapping("/invest/list")
    public InvestListRes list(HttpServletRequest request) {
        return windmillBorrowBiz.list(request);
    }

    @ApiOperation("标的投标记录")
    @RequestMapping("/borrow/info")
    public BorrowTenderList info(HttpServletRequest request) {
        return windmillBorrowBiz.tenderList(request);
    }

    @ApiOperation("查询某天投资情况")
    @RequestMapping("/invest/byTime/list")
    public BySomeDayRes byTime(HttpServletRequest request) {
        return windmillBorrowBiz.bySomeDayTenders(request);
    }

    @ApiOperation("查询每日的汇总数据")
    @RequestMapping("/invest/bySomeday/list")
    public ByDayStatistics bySomeday(HttpServletRequest request) {
        return windmillStatisticsBiz.byDayStatistics(request);
    }



}
