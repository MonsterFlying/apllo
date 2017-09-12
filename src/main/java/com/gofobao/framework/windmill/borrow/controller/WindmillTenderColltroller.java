package com.gofobao.framework.windmill.borrow.controller;

import com.gofobao.framework.windmill.borrow.biz.WindmillTenderBiz;
import com.gofobao.framework.windmill.borrow.vo.response.BackRecordsRes;
import com.gofobao.framework.windmill.borrow.vo.response.InvestRecordsRes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by admin on 2017/8/4.
 */
@RestController
@RequestMapping("/pub/windmill")
@Slf4j
@ApiModel(description = "投资")
public class WindmillTenderColltroller {

    @Autowired
    private WindmillTenderBiz windmillTenderBiz;

    @ApiOperation("用户投资回款查询")
    @RequestMapping("/user/payBack/list")
    public BackRecordsRes payBacK(HttpServletRequest request) {
        return windmillTenderBiz.backRecordList(request) ;
    }

    @ApiOperation("用户投资记录查询接口")
    @RequestMapping("user/invest/list")
    public InvestRecordsRes userInvestList(HttpServletRequest request) {
        return windmillTenderBiz.investRecordList(request);   }


}
