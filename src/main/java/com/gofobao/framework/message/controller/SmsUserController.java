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
    private MessageBiz messageBiz;

    @ApiOperation("发送银行存管开户短信")
    @PostMapping("/openAccount")
    public ResponseEntity<VoBaseResp> openAccount(HttpServletRequest request,
                                                  @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                  @Valid @ModelAttribute VoAnonSmsReq voAnonSmsReq) {
        ResponseEntity<VoBaseResp> result = messageBiz.openAccount(userId, voAnonSmsReq);
        return result;
    }

    @ApiOperation("在线联机充值短信发送")
    @PostMapping("/rechargeOnline")
    public ResponseEntity<VoBaseResp> rechargeOnline(HttpServletRequest request,
                                                     @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                     @Valid @ModelAttribute VoUserSmsReq voUserSmsReq) {
        voUserSmsReq.setUserId(userId);
        ResponseEntity<VoBaseResp> result = messageBiz.rechargeOnline(request, voUserSmsReq);
        return result;
    }

    @ApiOperation("更换手机---> 发送解绑手机验证码  第一步")
    @PostMapping("/switch/phone/unbind")
    public ResponseEntity<VoBaseResp> switchPhone(HttpServletRequest request,
                                                  @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                  @Valid @ModelAttribute VoUserSmsReq voUserSmsReq) {
        voUserSmsReq.setUserId(userId);
        return messageBiz.sendSwitchPhone(request, voUserSmsReq);
    }


    @ApiOperation("绑定新手---> 发送用户绑定手机")
    @PostMapping("/bindPhone")
    public ResponseEntity<VoBaseResp> sendBindPhone(HttpServletRequest request,
                                                    @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                    @Valid @ModelAttribute VoAnonSmsReq voAnonSmsReq) {
        return messageBiz.sendBindPhone(request, voAnonSmsReq, userId);
    }


    @ApiOperation("更换手机---> 发送绑定新手机短信验证码 ")
    @PostMapping("/switch/newPhone/bind")
    public ResponseEntity<VoBaseResp> sendBindPhone4Switch(HttpServletRequest request,
                                                           @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                           @Valid @ModelAttribute VoAnonSmsReq voAnonSmsReq) {
        return messageBiz.sendBindPhone4Switch(request, voAnonSmsReq, userId);
    }

    @ApiOperation("重置交易密码-->发送短信验证码")
    @PostMapping("/rest/payPassWord/sendSms")
    public ResponseEntity<VoBaseResp> restPayPassWord(HttpServletRequest request,
                                                      @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                      @Valid @ModelAttribute VoUserSmsReq voUserSmsReq) {
        voUserSmsReq.setUserId(userId);
        return messageBiz.sendRestTranPassWord(request, voUserSmsReq);
    }

}
