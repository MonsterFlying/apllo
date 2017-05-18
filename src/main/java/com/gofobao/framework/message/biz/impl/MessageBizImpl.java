package com.gofobao.framework.message.biz.impl;

import com.gofobao.framework.core.ons.config.OnsBodyKeys;
import com.gofobao.framework.core.ons.config.OnsMessage;
import com.gofobao.framework.core.ons.config.OnsTags;
import com.gofobao.framework.core.ons.config.OnsTopics;
import com.gofobao.framework.core.ons.helper.ApolloMQHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.CaptchaHelper;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.message.biz.MessageBiz;
import com.gofobao.framework.message.vo.VoSmsReq;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Max on 17/5/17.
 */
@Service
@Slf4j
public class MessageBizImpl implements MessageBiz {

    @Autowired
    UserService userService ;

    @Autowired
    CaptchaHelper captchaHelper ;

    @Autowired
    ApolloMQHelper apolloMQHelper ;

    @Override
    public ResponseEntity<VoBaseResp> sendRegisterCode(ServletRequest request, VoSmsReq voSmsReq) {
        // 验证短信用户是否
        boolean match = captchaHelper.match(voSmsReq.getCaptchaToken(), voSmsReq.getCaptcha());
        if(!match){
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "图形验证码错误或者已过期"));
        }

        // 查询用户是否唯一
        boolean notExistsState = userService.notExistsByPhone(voSmsReq.getPhone()) ;

        if(!notExistsState) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "手机号已经注册"));
        }


        OnsMessage onsMessage = new OnsMessage();
        onsMessage.setTopic(OnsTopics.TOPIC_SMS);
        onsMessage.setTag(OnsTags.SMS_REGISTER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(OnsBodyKeys.KEYS_PHONE, voSmsReq.getPhone(), OnsBodyKeys.KEYS_IP, request.getRemoteAddr()) ;

        Gson gson = new Gson() ;
        onsMessage.setBody(gson.toJson(body));
        boolean state = apolloMQHelper.send(onsMessage) ;

        if(!state) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍候重试！"));
        }

        // 调用MQ 发送注册短信
        return ResponseEntity.ok(VoBaseResp.ok("短信发送成功"));
    }

    @Override
    public ResponseEntity<VoBaseResp> sendFindPassword(HttpServletRequest request, VoSmsReq voSmsReq) {
        // 验证短信用户是否
        boolean match = captchaHelper.match(voSmsReq.getCaptchaToken(), voSmsReq.getCaptcha());
        if(!match){
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "图形验证码错误或者已过期"));
        }

        // 查询用户是否存在
        boolean notExistsState = userService.notExistsByPhone(voSmsReq.getPhone()) ;

        if(notExistsState) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机号未在平台注册"));
        }


        OnsMessage onsMessage = new OnsMessage();
        onsMessage.setTopic(OnsTopics.TOPIC_SMS);
        onsMessage.setTag(OnsTags.SMS_RESET_PASSWORD);
        ImmutableMap<String, String> body = ImmutableMap
                .of(OnsBodyKeys.KEYS_PHONE, voSmsReq.getPhone(), OnsBodyKeys.KEYS_IP, request.getRemoteAddr()) ;

        Gson gson = new Gson() ;
        onsMessage.setBody(gson.toJson(body));
        boolean state = apolloMQHelper.send(onsMessage) ;

        if(!state) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍候重试！"));
        }

        // 调用MQ 发送注册短信
        return ResponseEntity.ok(VoBaseResp.ok("短信发送成功"));
    }

    /**
     * 发送更换手机号码短信验证码
     * @param request 请求类
     * @param voSmsReq 消息体
     * @return
     */
    public ResponseEntity<VoBaseResp> sendSwitchPhone(HttpServletRequest request, VoSmsReq voSmsReq){
        // 验证短信用户是否
        boolean match = captchaHelper.match(voSmsReq.getCaptchaToken(), voSmsReq.getCaptcha());
        if(!match){
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "图形验证码错误或者已过期"));
        }

        // 查询用户是否存在
        boolean notExistsState = userService.notExistsByPhone(voSmsReq.getPhone()) ;

        if(notExistsState) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机号未在平台注册"));
        }

        OnsMessage onsMessage = new OnsMessage();
        onsMessage.setTopic(OnsTopics.TOPIC_SMS);
        onsMessage.setTag(OnsTags.SMS_SWICTH_PHONE);
        ImmutableMap<String, String> body = ImmutableMap
                .of(OnsBodyKeys.KEYS_PHONE, voSmsReq.getPhone(), OnsBodyKeys.KEYS_IP, request.getRemoteAddr()) ;

        Gson gson = new Gson() ;
        onsMessage.setBody(gson.toJson(body));
        boolean state = apolloMQHelper.send(onsMessage) ;

        if(!state) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍候重试！"));
        }

        // 调用MQ 发送注册短信
        return ResponseEntity.ok(VoBaseResp.ok("短信发送成功"));
    }

    /**
     * 发送更换手机号码短信验证码
     * @param request 请求类
     * @param voSmsReq 消息体
     * @return
     */
    public ResponseEntity<VoBaseResp> sendBindPhone(HttpServletRequest request, VoSmsReq voSmsReq){
        // 验证短信用户是否
        boolean match = captchaHelper.match(voSmsReq.getCaptchaToken(), voSmsReq.getCaptcha());
        if(!match){
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "图形验证码错误或者已过期"));
        }

        // 查询用户是否存在
        boolean notExistsState = userService.notExistsByPhone(voSmsReq.getPhone()) ;

        if(notExistsState) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机号未在平台注册"));
        }

        OnsMessage onsMessage = new OnsMessage();
        onsMessage.setTopic(OnsTopics.TOPIC_SMS);
        onsMessage.setTag(OnsTags.SMS_BUNDLE);
        ImmutableMap<String, String> body = ImmutableMap
                .of(OnsBodyKeys.KEYS_PHONE, voSmsReq.getPhone(), OnsBodyKeys.KEYS_IP, request.getRemoteAddr()) ;

        Gson gson = new Gson() ;
        onsMessage.setBody(gson.toJson(body));
        boolean state = apolloMQHelper.send(onsMessage) ;

        if(!state) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍候重试！"));
        }

        // 调用MQ 发送注册短信
        return ResponseEntity.ok(VoBaseResp.ok("短信发送成功"));
    }
}
