package com.gofobao.framework.member.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by Administrator on 2017/6/20 0020.
 */
@Controller
public class RegisterWebController {

    @ApiOperation("用户注册协议")
    @GetMapping("/pub/register/protocol")
    public String registerProtocol(){
        return "/register/registerProtocol" ;
    }

}
