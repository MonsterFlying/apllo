package com.gofobao.framework.member.controller;

import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.member.vo.request.VoOpenAccountReq;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.member.vo.response.VoOpenAccountResp;
import com.gofobao.framework.member.vo.response.VoPreOpenAccountResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 存管账户
 * Created by Max on 17/5/22.
 */
@RestController
public class UserThirdController {
    @Autowired
    private UserThirdBiz userThirdBiz ;


    @ApiOperation("银行存管前置请求第一步")
    @PostMapping("/user/third/preOpenAccout")
    public ResponseEntity<VoPreOpenAccountResp> preOpenAccout(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.preOpenAccount(userId)  ;
    }


    @ApiOperation("银行存管开户")
    @PostMapping("/user/third/openAccout")
    public ResponseEntity<VoOpenAccountResp> openAccount(HttpServletRequest httpServletRequest, @Valid @ModelAttribute VoOpenAccountReq voOpenAccountReq, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.openAccount(voOpenAccountReq, userId, httpServletRequest) ;
    }



    @ApiOperation("银行存管密码管理")
    @PostMapping("/user/third/modifyOpenAccPwd")
    public ResponseEntity<VoHtmlResp> modifyOpenAccPwd(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.modifyOpenAccPwd(httpServletRequest, userId) ;
    }

    @ApiOperation("银行存管密码管理回调")
    @PostMapping("/pub/user/third/modifyOpenAccPwd/callback/{type}")
    public ResponseEntity<String> modifyOpenAccPwdCallback(HttpServletRequest request, HttpServletResponse response, @PathVariable Integer type) {
        return userThirdBiz.modifyOpenAccPwdCallback(request, response, type) ;
    }


    @ApiOperation("开通自动投标协议")
    @PostMapping("/user/third/autoTender")
    public ResponseEntity<VoHtmlResp> autoTender(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.autoTender(httpServletRequest, userId) ;
    }

    @ApiOperation("开通自动转让协议")
    @PostMapping("/user/third/autoTranfter")
    public ResponseEntity<VoHtmlResp> autoTranfter(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.autoTranfter(httpServletRequest, userId) ;
    }


    @ApiOperation("开通自动投标协议回调")
    @PostMapping("/pub/user/third/autoTender/callback")
    public ResponseEntity<String> autoTenderCallback(HttpServletRequest request, HttpServletResponse response) {
        return userThirdBiz.autoTenderCallback(request, response) ;
    }


    @ApiOperation("开通自动投标协议回调")
    @PostMapping("/pub/user/third/autoTranfer/callback")
    public ResponseEntity<String> autoTranferCallback(HttpServletRequest request, HttpServletResponse response) {
        return userThirdBiz.autoTranferCallback(request, response) ;
    }
}
