package com.gofobao.framework.message.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.biz.MessageBiz;
import com.gofobao.framework.message.vo.VoSmsReq;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 短信
 * Created by Max on 17/5/17.
 */
@RestController
public class SmsAnonymousController {

    @Autowired
    private MessageBiz messageBiz ;


    @ApiOperation("发送注册短信")
    @GetMapping("/anonymous/sms/register")
    public ResponseEntity<VoBaseResp> register(HttpServletRequest request, @Valid @ModelAttribute VoSmsReq voSmsReq) {
        return messageBiz.sendRegisterCode(request, voSmsReq) ;
    }

}
