package com.gofobao.framework.message.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.CaptchaHelper;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.message.biz.MessageBiz;
import com.gofobao.framework.message.vo.VoSmsReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.ServletRequest;

/**
 * Created by Max on 17/5/17.
 */
@Service
public class MessageBizImpl implements MessageBiz {

    @Autowired
    UserService userService ;

    @Autowired
    CaptchaHelper captchaHelper ;

    @Override
    public ResponseEntity<VoBaseResp> sendRegisterCode(ServletRequest request, VoSmsReq voSmsReq) {
        // 验证短信用户是否
        boolean match = captchaHelper.match(voSmsReq.getPhone(), voSmsReq.getCaptcha());
        if(!match){
            return ResponseEntity.badRequest().body(null);
        }

        return null;
    }
}
