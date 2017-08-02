package com.gofobao.framework.windmill.borrow.controller;

import com.gofobao.framework.windmill.borrow.biz.WindmillBorrowBiz;
import com.gofobao.framework.windmill.borrow.vo.response.InvestListRes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
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
    private WindmillBorrowBiz borrowBiz;

    @ApiOperation("标列表")
    @GetMapping("/invest/list")
    public InvestListRes list(HttpServletRequest request, @Param("borrowId") Long id) {
        return borrowBiz.list(id);
    }

    @ApiOperation("根据时间段查询用户的投资列表")
    @GetMapping("/invest/byTime/list")
    public String byTime(HttpServletRequest request) {

        return "";
    }

    @ApiOperation("根据某天查询用户投资列表")
    @GetMapping("/invest/bySomeday/list")
    public String bySomeday(HttpServletRequest request) {
        return "";
    }


    @ApiOperation("用户投资记录查询接口")
    @PostMapping("user/invest/list")
    public String userInvestList(HttpServletRequest request) {
        return "";

    }


}
