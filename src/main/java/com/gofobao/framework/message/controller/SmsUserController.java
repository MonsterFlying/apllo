package com.gofobao.framework.message.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.biz.MessageBiz;
import com.gofobao.framework.message.vo.VoUserSmsReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 短信发送（需要登录情况下）
 * Created by Max on 17/5/17.
 */
@RestController
@RequestMapping("/user")
public class SmsUserController {

    @Autowired
    private MessageBiz messageBiz ;

    @ApiOperation("发送银行存管开户短信")
    @PostMapping("/sms/openAccount")
    public ResponseEntity<VoBaseResp> findPassword(@RequestAttribute(SecurityContants.USERID_KEY) long userId, @Valid @ModelAttribute VoUserSmsReq voUserSmsReq){
        voUserSmsReq.setUserId(userId);
        ResponseEntity<VoBaseResp> result = messageBiz.openAccount(voUserSmsReq) ;
        return result;
    }
}
