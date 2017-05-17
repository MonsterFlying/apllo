package com.gofobao.framework.message.biz.impl;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
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

    @Autowired
    ProducerBean smsProducerBean ;

    @Override
    public ResponseEntity<VoBaseResp> sendRegisterCode(ServletRequest request, VoSmsReq voSmsReq) {
        // 验证短信用户是否
        boolean match = captchaHelper.match(voSmsReq.getPhone(), voSmsReq.getCaptcha());
        if(!match){
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "图形验证码错误，请重新提交"));
        }

        // 查询用户是否唯一
        boolean only = userService.phoneIsOnly(voSmsReq.getPhone()) ;

        if(!only){
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "手机号已经注册"));
        }
        Message message = new Message() ;
        SendResult send = smsProducerBean.send(message) ;
        // 调用MQ 发送注册短信
        return ResponseEntity.ok(VoBaseResp.ok("短信发送成功"));
    }
}
