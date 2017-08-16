package com.gofobao.framework.security.controller.finance;

import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.security.vo.VoLoginReq;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by admin on 2017/8/15.
 */
/**
 * 权限验证模块
 * Created by Max on 2017/5/16.
 */
@RestController
@Api(description = "理财登录")
@RequestMapping("/pub/auth/finance")
public class FinanceAuthenticationRestController {

    @Autowired
    UserBiz userBiz ;

    @PostMapping("/login")
    public ResponseEntity<VoBasicUserInfoResp> login(HttpServletRequest httpServletRequest, HttpServletResponse response, @ModelAttribute VoLoginReq voLoginReq){
        return userBiz.financeLogin(httpServletRequest, response, voLoginReq) ;
    }


}
