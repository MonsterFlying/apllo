package com.gofobao.framework.member.controller.finance;

import com.gofobao.framework.core.vo.VoBaseResp;
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
import javax.validation.Valid;

/**
 * 存管账户
 * Created by Max on 17/5/22.
 */
@RestController
public class FinanceUserThirdController {
    @Autowired
    private UserThirdBiz userThirdBiz ;


    @ApiOperation("银行存管前置请求第一步")
    @PostMapping("/user/finance/v2/third/preOpenAccount")
    public ResponseEntity<VoPreOpenAccountResp> preOpenAccout(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.preOpenAccount(userId)  ;
    }


    @ApiOperation("银行存管开户")
    @PostMapping("/user/finance/v2/third/openAccount")
    public ResponseEntity<VoOpenAccountResp> openAccount(HttpServletRequest httpServletRequest, @Valid @ModelAttribute VoOpenAccountReq voOpenAccountReq, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.openAccount(voOpenAccountReq, userId, httpServletRequest) ;
    }


    @ApiOperation("银行存管密码管理")
    @PostMapping("/user/finance/v2/third/modifyOpenAccPwd")
    public ResponseEntity<VoHtmlResp> modifyOpenAccPwd(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.modifyOpenAccPwd(httpServletRequest, userId) ;
    }


    @ApiOperation("绑定银行卡")
    @GetMapping("/user/finance/v2/third/bind/bank/{bankNo}")
    public ResponseEntity<VoHtmlResp> bindBank(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId,@PathVariable("bankNo") String bankNo) {
        return userThirdBiz.bindBank(httpServletRequest, userId, bankNo) ;
    }


    @ApiOperation("解除银行卡绑定")
    @PostMapping("/user/finance/v2/third/del/bank")
    public ResponseEntity<VoBaseResp> delBank(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.delBank(httpServletRequest, userId) ;
    }


    @ApiOperation("开通自动投标协议")
    @PostMapping("/user/finance/v2/third/autoTender")
    public ResponseEntity<VoHtmlResp> autoTender(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.autoTender(httpServletRequest, userId) ;
    }

    @ApiOperation("开通自动转让协议")
    @PostMapping("/user/finance/v2/third/autoTranfter")
    public ResponseEntity<VoHtmlResp> autoTranfter(HttpServletRequest httpServletRequest, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.autoTranfter(httpServletRequest, userId) ;
    }


}
