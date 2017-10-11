package com.gofobao.framework.starfire.user.controller;

import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.starfire.user.biz.StarFireUserBiz;
import com.gofobao.framework.starfire.user.vo.request.*;
import com.gofobao.framework.starfire.user.vo.response.FetchLoginTokenRes;
import com.gofobao.framework.starfire.user.vo.response.RegisterQueryRes;
import com.gofobao.framework.starfire.user.vo.response.RegisterRes;
import com.gofobao.framework.starfire.user.vo.response.UserAccountRes;
import com.gofobao.framework.windmill.user.vo.request.BindLoginReq;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by master on 2017/9/26.
 */

@RestController
@RequestMapping("pub/starfire/user")
public class StarFireUserController {

    @Autowired
    private StarFireUserBiz starFireUserBiz;

    @Value("${gofobao.h5Domain}")
    private String h5Domain;

    @Value("${gofobao.pcDomain}")
    private String pcDomain;

    @Autowired
    private ThymeleafHelper thymeleafHelper;

    @Value("${gofobao.javaDomain}")
    private String javaDomain;

    @RequestMapping(value = "registerQuery", method = RequestMethod.POST)
    @ApiOperation("注册绑定查询接口")
    public RegisterQueryRes registerQuery(RegisterQuery registerQuery) {
        return starFireUserBiz.registerQuery(registerQuery);
    }

    @RequestMapping(value = "register", method = RequestMethod.POST)
    @ApiOperation("新用户注册接口")
    public RegisterRes register(RegisterModel registerModel, HttpServletRequest request) {
        return starFireUserBiz.register(registerModel, request);
    }

    @RequestMapping(value = "bind/html")
    @ApiOperation("绑定接口")
    public void loginHtml(BindUserModel bindUserModel, HttpServletResponse response) {
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("params", new Gson().toJson(bindUserModel));
        paramMap.put("address", javaDomain);
        try {
            //TODO 以后该外网地址
         //   response.sendRedirect(thymeleafHelper.build(pcDomain + "/starfire/user/login", paramMap));
            String loginUrl= pcDomain + "/third/xhzlogin?params="+new Gson().toJson(bindUserModel);
            response.sendRedirect(loginUrl);
        } catch (Exception e) {
            return;//thymeleafHelper.build("load_error", null);
        }
        return;
    }

    @RequestMapping(value = "bind/login", method = RequestMethod.POST)
    @ApiOperation("绑定用户登录接口")
    public ResponseEntity<VoBasicUserInfoResp> loginBind(HttpServletRequest request,
                                                         HttpServletResponse response,
                                                         BindLoginReq bindLoginReq) {
        return starFireUserBiz.bindLogin(request, response, bindLoginReq);
    }

    @RequestMapping(value = "fetchLoginToken", method = RequestMethod.POST)
    @ApiOperation("获取登录token")
    public FetchLoginTokenRes fetchLoginToken(FetchLoginToken fetchLoginToken) {
        return starFireUserBiz.fetchLoginToken(fetchLoginToken);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation("授权登陆")
    public void requestUrl(LoginModel loginModel, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(starFireUserBiz.requestUrl(loginModel, request, response));
    }

    @RequestMapping(value = "account", method = RequestMethod.POST)
    public UserAccountRes userAccount(UserAccount userAccount) {
        return starFireUserBiz.userAccount(userAccount);
    }


}
