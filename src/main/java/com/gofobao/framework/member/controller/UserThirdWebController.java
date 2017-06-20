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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 存管账户
 * Created by Max on 17/5/22.
 */
@Controller
public class UserThirdWebController {
    @Autowired
    private UserThirdBiz userThirdBiz ;

    @GetMapping("/pub/password/show/{id}")
    public String shwoPassword(@PathVariable("id") Long id, Model model) {
        return userThirdBiz.shwoPassword(id, model)  ;
    }


    @GetMapping("/pub/autoTender/show/{id}")
    public String showAutoTender(@PathVariable("id") Long id, Model model) {
        return userThirdBiz.showAutoTender(id, model)  ;
    }

    @GetMapping("/pub/autoTranfer/show/{id}")
    public String showAutoTranfer(@PathVariable("id") Long id, Model model) {
        return userThirdBiz.showAutoTranfer(id, model)  ;
    }


    @ApiOperation("江西银行网络交易资金账户服务三方协议")
    @GetMapping("/thirdAccount/protocol")
    public String thirdAccountProtocol(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, Model model) {
        userThirdBiz.thirdAccountProtocol(userId, model) ;
        return "thirdAccount/thirdAccountProtocol"  ;
    }

}
