package com.gofobao.framework.message.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.biz.MessageBiz;
import com.gofobao.framework.message.vo.VoAnonSmsReq;
import com.gofobao.framework.message.vo.VoUserSmsReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 短信发送（需要登录情况下）
 * Created by Max on 17/5/17.
 */
@RestController
@RequestMapping("/sms")
public class SmsUserController {

    @Autowired
    private MessageBiz messageBiz ;

    @ApiOperation("发送银行存管开户短信")
    @PostMapping("/openAccount")
    public ResponseEntity<VoBaseResp> openAccount(HttpServletRequest request, @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoUserSmsReq voUserSmsReq){
        voUserSmsReq.setUserId(userId);
        ResponseEntity<VoBaseResp> result = messageBiz.openAccount(voUserSmsReq) ;
        return result;
    }


    @ApiOperation("发送开通自动投标协议短息")
    @PostMapping("/openAutoTender")
    public ResponseEntity<VoBaseResp> openAutoTender(HttpServletRequest request, @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoUserSmsReq voUserSmsReq){
        voUserSmsReq.setUserId(userId);
        ResponseEntity<VoBaseResp> result = messageBiz.openAutoTender(voUserSmsReq) ;
        return result;
    }

    @ApiOperation("发送解绑手机验证码")
    @PostMapping("/switchPhone")
    public ResponseEntity<VoBaseResp> switchPhone(HttpServletRequest request,  @RequestAttribute(SecurityContants.USERID_KEY) Long userId,  @Valid @ModelAttribute VoUserSmsReq voUserSmsReq){
        voUserSmsReq.setUserId(userId);
        return messageBiz.sendSwitchPhone(request, voUserSmsReq) ;
    }

    @ApiOperation("发送绑定手机号码短信验证码")
    @PostMapping("/bindPhone")
    public ResponseEntity<VoBaseResp> sendBindPhone(HttpServletRequest request,  @RequestAttribute(SecurityContants.USERID_KEY) Long userId,  @Valid @ModelAttribute VoAnonSmsReq voAnonSmsReq){
        return messageBiz.sendBindPhone(request, voAnonSmsReq, userId) ;
    }
}
