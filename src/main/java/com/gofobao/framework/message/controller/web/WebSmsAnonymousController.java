package com.gofobao.framework.message.controller.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.message.biz.MessageBiz;
import com.gofobao.framework.message.biz.SmsNoticeSettingsBiz;
import com.gofobao.framework.message.vo.request.VoAnonSmsReq;
import io.swagger.annotations.Api;
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
 * 短信
 * Created by Max on 17/5/17.
 */
@RestController
@RequestMapping("/pub")
@Api(description="pc：注册短信")
public class WebSmsAnonymousController {

    @Autowired
    private MessageBiz messageBiz ;
    @Autowired
    private SmsNoticeSettingsBiz smsNoticeSettingsBiz;

    @ApiOperation("pc：发送注册短信验证码")
    @PostMapping("pc/v2/sms/register")
    public ResponseEntity<VoBaseResp> register(HttpServletRequest request, @Valid @ModelAttribute VoAnonSmsReq voAnonSmsReq) {
        return messageBiz.sendRegisterCode(request, voAnonSmsReq) ;
    }

    @ApiOperation("PC:发送忘记密码短信验证码")
    @PostMapping("pc/v2/sms/findPassword")
    public ResponseEntity<VoBaseResp> findPassword(HttpServletRequest request, @Valid @ModelAttribute VoAnonSmsReq voAnonSmsReq){
        return messageBiz.sendFindPassword(request, voAnonSmsReq) ;
    }
}
