package com.gofobao.framework.message.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.biz.MessageBiz;
import com.gofobao.framework.message.vo.VoAnonSmsReq;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 短信
 * Created by Max on 17/5/17.
 */
@RestController
@RequestMapping("/pub")
public class SmsAnonymousController {

    @Autowired
    private MessageBiz messageBiz ;


    @ApiOperation("发送注册短信验证码")
    @PostMapping("/v2/sms/register")
    public ResponseEntity<VoBaseResp> register(HttpServletRequest request, @Valid @ModelAttribute VoAnonSmsReq voAnonSmsReq) {
        return messageBiz.sendRegisterCode(request, voAnonSmsReq) ;
    }

    @ApiOperation("发送忘记密码短信验证码")
    @PostMapping("/v2/sms/findPassword")
    public ResponseEntity<VoBaseResp> findPassword(HttpServletRequest request, @Valid @ModelAttribute VoAnonSmsReq voAnonSmsReq){
        return messageBiz.sendFindPassword(request, voAnonSmsReq) ;
    }
}
