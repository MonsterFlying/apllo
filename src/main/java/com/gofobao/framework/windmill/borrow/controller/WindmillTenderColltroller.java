package com.gofobao.framework.windmill.borrow.controller;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
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


    @ApiOperation("用户投资回款查询")
    @PostMapping("/user/payBack/list")
    public String payBacK(HttpServletRequest request){

        return "";
    }



}
