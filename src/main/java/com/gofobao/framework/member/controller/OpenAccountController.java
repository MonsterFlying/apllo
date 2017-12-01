package com.gofobao.framework.member.controller;

import com.gofobao.framework.member.biz.OpenAccountBiz;
import com.gofobao.framework.member.vo.response.VoAccountStatusResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(description = "开户模块")
@Controller
public class OpenAccountController {

    @Autowired
    OpenAccountBiz openAccountBiz;

    @GetMapping("/pub/openAccount/callback/{userId}/{process}")
    public String opeanAccountCallBack(
            @PathVariable Long userId,
            @PathVariable String process,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            Model model) {
        return openAccountBiz.opeanAccountCallBack(userId, process, httpServletRequest, httpServletResponse, model);
    }


    @ApiOperation("开户密码管理")
    @GetMapping("/account/password-management")
    public ResponseEntity<VoHtmlResp> accountPasswordManagement(HttpServletRequest httpServletRequest,
                                                                @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return openAccountBiz.accountPasswordManagement(httpServletRequest, userId);
    }

    @ApiOperation("平台投标授权")
    @GetMapping("/account/tender-authorize/{msgCode}")
    ResponseEntity<VoHtmlResp> acocuntAuthorizeTender(HttpServletRequest httpServletRequest,
                                                      @PathVariable @NonNull String msgCode,
                                                      @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return openAccountBiz.acocuntAuthorizeTender(httpServletRequest, msgCode, userId);
    }


    @ApiOperation("债权转让授权")
    @GetMapping("/account/transfer-authorize/{msgCode}")
    ResponseEntity<VoHtmlResp> acocuntAuthorizeTransfer(HttpServletRequest httpServletRequest,
                                                      @PathVariable @NonNull String msgCode,
                                                      @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return openAccountBiz.acocuntAuthorizeTransfer(httpServletRequest, msgCode, userId);
    }

    @ApiOperation("查询账户开户信心")
    @GetMapping("/account/acocunt-config-info")
    ResponseEntity<VoAccountStatusResp> acocuntConfigState(HttpServletRequest httpServletRequest,
                                                                 @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return openAccountBiz.acocuntConfigState(httpServletRequest, userId);
    }


}
