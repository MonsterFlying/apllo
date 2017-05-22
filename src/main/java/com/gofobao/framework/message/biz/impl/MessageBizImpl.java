package com.gofobao.framework.message.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.contants.SrvTxCodeContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.smscodeapply.SmsCodeApplyRequest;
import com.gofobao.framework.api.model.smscodeapply.SmsCodeApplyResponse;
import com.gofobao.framework.core.ons.config.OnsBodyKeys;
import com.gofobao.framework.core.ons.config.OnsMessage;
import com.gofobao.framework.core.ons.config.OnsTags;
import com.gofobao.framework.core.ons.config.OnsTopics;
import com.gofobao.framework.core.ons.helper.ApolloMQHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.CaptchaHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.message.biz.MessageBiz;
import com.gofobao.framework.message.vo.VoAnonSmsReq;
import com.gofobao.framework.message.vo.VoUserSmsReq;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

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

    @Autowired
    JixinManager jixinManager ;

    @Autowired
    RedisHelper redisHelper ;

    @Override
    public ResponseEntity<VoBaseResp> sendRegisterCode(ServletRequest request, VoAnonSmsReq voAnonSmsReq) {
        // 验证短信用户是否
        boolean match = captchaHelper.match(voAnonSmsReq.getCaptchaToken(), voAnonSmsReq.getCaptcha());
        if(!match){
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "图形验证码错误或者已过期"));
        }

        // 查询用户是否唯一
        boolean notExistsState = userService.notExistsByPhone(voAnonSmsReq.getPhone()) ;

        if(!notExistsState) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "手机号已经注册"));
        }


        OnsMessage onsMessage = new OnsMessage();
        onsMessage.setTopic(OnsTopics.TOPIC_SMS);
        onsMessage.setTag(OnsTags.SMS_REGISTER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(OnsBodyKeys.KEYS_PHONE, voAnonSmsReq.getPhone(), OnsBodyKeys.KEYS_IP, request.getRemoteAddr()) ;

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
    public ResponseEntity<VoBaseResp> sendFindPassword(HttpServletRequest request, VoAnonSmsReq voAnonSmsReq) {
        // 验证短信用户是否
        boolean match = captchaHelper.match(voAnonSmsReq.getCaptchaToken(), voAnonSmsReq.getCaptcha());
        if(!match){
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "图形验证码错误或者已过期"));
        }

        // 查询用户是否存在
        boolean notExistsState = userService.notExistsByPhone(voAnonSmsReq.getPhone()) ;

        if(notExistsState) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机号未在平台注册"));
        }


        OnsMessage onsMessage = new OnsMessage();
        onsMessage.setTopic(OnsTopics.TOPIC_SMS);
        onsMessage.setTag(OnsTags.SMS_RESET_PASSWORD);
        ImmutableMap<String, String> body = ImmutableMap
                .of(OnsBodyKeys.KEYS_PHONE, voAnonSmsReq.getPhone(), OnsBodyKeys.KEYS_IP, request.getRemoteAddr()) ;

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
     * @param voAnonSmsReq 消息体
     * @return
     */
    public ResponseEntity<VoBaseResp> sendSwitchPhone(HttpServletRequest request, VoAnonSmsReq voAnonSmsReq){
        // 验证短信用户是否
        boolean match = captchaHelper.match(voAnonSmsReq.getCaptchaToken(), voAnonSmsReq.getCaptcha());
        if(!match){
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "图形验证码错误或者已过期"));
        }

        // 查询用户是否存在
        boolean notExistsState = userService.notExistsByPhone(voAnonSmsReq.getPhone()) ;

        if(notExistsState) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机号未在平台注册"));
        }

        OnsMessage onsMessage = new OnsMessage();
        onsMessage.setTopic(OnsTopics.TOPIC_SMS);
        onsMessage.setTag(OnsTags.SMS_SWICTH_PHONE);
        ImmutableMap<String, String> body = ImmutableMap
                .of(OnsBodyKeys.KEYS_PHONE, voAnonSmsReq.getPhone(), OnsBodyKeys.KEYS_IP, request.getRemoteAddr()) ;

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
     * @param voAnonSmsReq 消息体
     * @return
     */
    public ResponseEntity<VoBaseResp> sendBindPhone(HttpServletRequest request, VoAnonSmsReq voAnonSmsReq){
        // 验证短信用户是否
        boolean match = captchaHelper.match(voAnonSmsReq.getCaptchaToken(), voAnonSmsReq.getCaptcha());
        if(!match){
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "图形验证码错误或者已过期"));
        }

        // 查询用户是否存在
        boolean notExistsState = userService.notExistsByPhone(voAnonSmsReq.getPhone()) ;

        if(notExistsState) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机号未在平台注册"));
        }

        OnsMessage onsMessage = new OnsMessage();
        onsMessage.setTopic(OnsTopics.TOPIC_SMS);
        onsMessage.setTag(OnsTags.SMS_BUNDLE);
        ImmutableMap<String, String> body = ImmutableMap
                .of(OnsBodyKeys.KEYS_PHONE, voAnonSmsReq.getPhone(), OnsBodyKeys.KEYS_IP, request.getRemoteAddr()) ;

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
    public ResponseEntity<VoBaseResp> openAccount(VoUserSmsReq voUserSmsReq) {
        // 1.验证图形验证码
        boolean match = captchaHelper.match(voUserSmsReq.getCaptchaToken(), voUserSmsReq.getCaptcha());
        if(!match){
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "图形验证码错误或者已过期"));
        }

        // 2.判断用户是否存在
        Users users = userService.findById(voUserSmsReq.getUserId());

        if(ObjectUtils.isEmpty(users)){ // 判断用户是否存在
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户不存在！"));
        }

        // 3.判断用户是否绑定手机
        String mobile = users.getPhone() ;
        if(StringUtils.isEmpty(mobile)){
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户未绑定手机，请绑定手机！"));
        }


        // 4.请求即信发送验证码
        SmsCodeApplyRequest request = new SmsCodeApplyRequest() ;
        request.setSrvTxCode(SrvTxCodeContants.ACCOUNT_OPEN_PLUS) ;
        request.setMobile(mobile) ;
        request.setChannel(ChannelContant.HTML);

        SmsCodeApplyResponse body = jixinManager.send(JixinTxCodeEnum.SMS_CODE_APPLY, request, SmsCodeApplyResponse.class);

        if(ObjectUtils.isEmpty(body)){
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "当前通讯网络不稳定，请稍候重试！"));
        }

        if(!JixinResultContants.SUCCESS.equals(body.getRetCode())){
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, body.getRetMsg()));
        }

        // 5.将授权码放入redis中
        try {
            redisHelper.put(String.format("%s_%s", SrvTxCodeContants.ACCOUNT_OPEN_PLUS, mobile), body.getSrvAuthCode(), 15 * 60);
        } catch (Exception e) {
            log.error("即信授权码写入redis异常", e);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍候重试！"));
        }


        return ResponseEntity.ok(VoBaseResp.ok("短信发送成功"));

    }
}
