package com.gofobao.framework.system.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 关于我们
 * Created by Max on 17/5/22.
 */
@Controller
public class PublicWebController {
    @ApiOperation("关于我们")
    @GetMapping("/pub/aboutMe")
    public String aboutMe() {
        return "aboutWe/about" ;
    }


    @ApiOperation("提现说明")
    @GetMapping("/pub/cash/desc")
    public String cashDesc(){
        return "cash/cashDesc" ;
    }

}
