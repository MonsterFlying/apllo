package com.gofobao.framework.windmill.user.controller;


import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.windmill.user.biz.WindmillUserBiz;
import com.gofobao.framework.windmill.user.vo.request.BindLoginReq;
import com.gofobao.framework.windmill.user.vo.respones.UserRegisterRes;
import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by Zeke on 2017/5/18.
 */
@RestController
@RequestMapping("/pub/windmill")
@Slf4j
@ApiModel(description = "风车理财用户注册")
public class WindmillUserController {


    @Autowired
    private WindmillUserBiz windmillUserBiz;

    @Value("${gofobao.javaDomain}")
    private String javaDomain;


    @Autowired
    private ThymeleafHelper thymeleafHelper;

    @ApiOperation("用户注册")
    @GetMapping(value = "user/register")
    public UserRegisterRes register(HttpServletRequest request) throws Exception {
        return windmillUserBiz.register(request);

    }

    @ApiOperation("用户绑定请求登录页面")
    @GetMapping("user/bind/html")
    public String loginHtml(HttpServletRequest request) {
        String paramStr = request.getParameter("param");
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("param", paramStr);
        paramMap.put("address", javaDomain);
        return thymeleafHelper.build("windmill/user/login", paramMap);
    }

    @ApiOperation("綁定登陸")
    @GetMapping("user/bind/login")
    public ResponseEntity<VoBasicUserInfoResp> bindLogin(HttpServletRequest request,
                                                         HttpServletResponse response,
                                                         BindLoginReq bindLoginReq) {
        return windmillUserBiz.bindLogin(request, response, bindLoginReq);
    }

    @ApiOperation("用户登录")
    @GetMapping("/user/login")
    public void loginUser(HttpServletRequest request, HttpServletResponse response)throws Exception {
        response.sendRedirect(windmillUserBiz.login(request, response));
    }
}
