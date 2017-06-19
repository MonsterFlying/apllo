package com.gofobao.framework.message.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.biz.MessageBiz;
import com.gofobao.framework.message.vo.request.VoAnonSmsReq;
import com.gofobao.framework.message.vo.request.VoUserSmsReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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
    public ResponseEntity<VoBaseResp> openAccount(HttpServletRequest request, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoUserSmsReq voUserSmsReq){
        voUserSmsReq.setUserId(userId);
        ResponseEntity<VoBaseResp> result = messageBiz.openAccount(voUserSmsReq) ;
        return result;
    }


    @ApiOperation("发送开通自动投标协议短息")
    @PostMapping("/openAutoTender")
    public ResponseEntity<VoBaseResp> openAutoTender(HttpServletRequest request, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoUserSmsReq voUserSmsReq){
        voUserSmsReq.setUserId(userId);
        ResponseEntity<VoBaseResp> result = messageBiz.openAutoTender(voUserSmsReq) ;
        return result;
    }


    @ApiOperation("发送开通自动债权转让协议短信")
    @PostMapping("/openAutoTranfer")
    public ResponseEntity<VoBaseResp> openAutoTranfer(HttpServletRequest request, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoUserSmsReq voUserSmsReq){
        voUserSmsReq.setUserId(userId);
        ResponseEntity<VoBaseResp> result = messageBiz.openAutoTranfer(voUserSmsReq) ;
        return result;
    }


    @ApiOperation("充值短信发送")
    @PostMapping("/recharge")
    public ResponseEntity<VoBaseResp> recharge(HttpServletRequest request, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoUserSmsReq voUserSmsReq){
        voUserSmsReq.setUserId(userId);
        ResponseEntity<VoBaseResp> result = messageBiz.recharge(voUserSmsReq) ;
        return result;
    }

    @ApiOperation("发送解绑手机验证码")
    @PostMapping("/switchPhone")
    public ResponseEntity<VoBaseResp> switchPhone(HttpServletRequest request, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,  @Valid @ModelAttribute VoUserSmsReq voUserSmsReq){
        voUserSmsReq.setUserId(userId);
        return messageBiz.sendSwitchPhone(request, voUserSmsReq) ;
    }


    @ApiOperation("发送用户绑定手机")
    @PostMapping("/bindPhone")
    public ResponseEntity<VoBaseResp> sendBindPhone(HttpServletRequest request, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,  @Valid @ModelAttribute VoAnonSmsReq voAnonSmsReq){
        return messageBiz.sendBindPhone(request, voAnonSmsReq, userId) ;
    }


    @ApiOperation("发送用户跟换手机号的绑定新手机短信")
    @PostMapping("/switch/bindPhone")
    public ResponseEntity<VoBaseResp> sendBindPhone4Switch(HttpServletRequest request, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,  @Valid @ModelAttribute VoAnonSmsReq voAnonSmsReq){
        return messageBiz.sendBindPhone(request, voAnonSmsReq, userId) ;
    }

}
