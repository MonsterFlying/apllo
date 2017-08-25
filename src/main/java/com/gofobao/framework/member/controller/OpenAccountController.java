package com.gofobao.framework.member.controller;

import com.gofobao.framework.member.biz.OpenAccountBiz;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Api(description = "开户模块")
@RestController
public class OpenAccountController {

    @Autowired
    OpenAccountBiz openAccountBiz ;

    public String continueOpenAccount(@RequestAttribute(SecurityContants.USERID_KEY) Long userId, HttpServletRequest httpServletRequest, Model model) {
        return openAccountBiz.openAccount(userId, httpServletRequest, model) ;
    }
}
