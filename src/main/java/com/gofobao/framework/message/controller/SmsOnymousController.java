package com.gofobao.framework.message.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.biz.MessageBiz;
import com.gofobao.framework.message.vo.VoAnonSmsReq;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Created by Zeke on 2017/5/18.
 */
@RestController
@RequestMapping("/sms")
public class SmsOnymousController {

    @Autowired
    private MessageBiz messageBiz ;

    @ApiOperation("发送更换手机号码短信验证码")
    @PostMapping("/switchPhone")
    public ResponseEntity<VoBaseResp> switchPhone(HttpServletRequest request, @Valid @ModelAttribute VoAnonSmsReq voAnonSmsReq){
        return messageBiz.sendSwitchPhone(request, voAnonSmsReq) ;
    }

    @ApiOperation("发送绑定手机号码短信验证码")
    @PostMapping("/bindPhone")
    public ResponseEntity<VoBaseResp> sendBindPhone(HttpServletRequest request, @Valid @ModelAttribute VoAnonSmsReq voAnonSmsReq){
        return messageBiz.sendBindPhone(request, voAnonSmsReq) ;
    }
}
