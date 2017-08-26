package com.gofobao.framework.member.controller;

import com.gofobao.framework.member.biz.OpenAccountBiz;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(description = "开户模块")
@RestController
public class OpenAccountController {

    @Autowired
    OpenAccountBiz openAccountBiz ;

    @GetMapping("/pub/openAccount/callback/{userId}/{process}")
    public String opeanAccountCallBack(
            @PathVariable Long userId,
            @PathVariable String process,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            Model model) {
        return openAccountBiz.opeanAccountCallBack(userId, process,  httpServletRequest, httpServletResponse, model) ;
    }
}
