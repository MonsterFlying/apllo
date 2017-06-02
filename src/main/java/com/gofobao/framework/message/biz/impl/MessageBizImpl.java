package com.gofobao.framework.message.biz.impl;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.contants.SrvTxCodeContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.sms_code_apply.SmsCodeApplyRequest;
import com.gofobao.framework.api.model.sms_code_apply.SmsCodeApplyResponse;
import com.gofobao.framework.common.rabbitmq.MqConfig;
import com.gofobao.framework.common.rabbitmq.MqHelper;
import com.gofobao.framework.common.rabbitmq.MqQueueEnum;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.CaptchaHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.message.biz.MessageBiz;
import com.gofobao.framework.message.vo.VoAnonSmsReq;
import com.gofobao.framework.message.vo.VoUserSmsReq;
import com.google.common.collect.ImmutableMap;
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
    MqHelper apollomqHelper ;

    @Autowired
    JixinManager jixinManager ;

    @Autowired
    RedisHelper redisHelper ;


    @Override
    public ResponseEntity<VoBaseResp> sendRegisterCode(ServletRequest request, VoAnonSmsReq voAnonSmsReq) {
        // 查询用户是否唯一
        boolean notExistsState = userService.notExistsByPhone(voAnonSmsReq.getPhone()) ;

        if(!notExistsState) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "手机号已经注册"));
        }


        MqConfig config = new MqConfig() ;
        config.setQueue(MqQueueEnum.RABBITMQ_SMS);
        config.setTag(MqTagEnum.SMS_REGISTER);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.PHONE, voAnonSmsReq.getPhone(), MqConfig.IP, request.getRemoteAddr()) ;
        config.setMsg(body);

        boolean state = apollomqHelper.convertAndSend(config);
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
        // 查询用户是否存在
        boolean notExistsState = userService.notExistsByPhone(voAnonSmsReq.getPhone()) ;

        if(notExistsState) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机号未在平台注册"));
        }

        MqConfig config = new MqConfig() ;
        config.setQueue(MqQueueEnum.RABBITMQ_SMS);
        config.setTag(MqTagEnum.SMS_RESET_PASSWORD);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.PHONE, voAnonSmsReq.getPhone(), MqConfig.IP, request.getRemoteAddr()) ;
        config.setMsg(body);

        boolean state = apollomqHelper.convertAndSend(config);

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
     * @param voUserSmsReq 消息体
     * @return
     */
    public ResponseEntity<VoBaseResp> sendSwitchPhone(HttpServletRequest request, VoUserSmsReq voUserSmsReq){
        // 查询用户是否存在
        Users user = userService.findById(voUserSmsReq.getUserId()) ;

        if(ObjectUtils.isEmpty(user)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机号未在平台注册"));
        }

        if(StringUtils.isEmpty(user.getPhone())){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机"));
        }


        MqConfig config = new MqConfig() ;
        config.setQueue(MqQueueEnum.RABBITMQ_SMS);
        config.setTag(MqTagEnum.SMS_SWICTH_PHONE);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.PHONE, user.getPhone(), MqConfig.IP, request.getRemoteAddr()) ;
        config.setMsg(body);

        boolean state = apollomqHelper.convertAndSend(config);

        if(!state) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍候重试！"));
        }

        // 调用MQ 发送注册短信
        return ResponseEntity.ok(VoBaseResp.ok("短信发送成功"));
    }

    @Override
    public ResponseEntity<VoBaseResp> sendBindPhone(HttpServletRequest request, VoAnonSmsReq voAnonSmsReq, Long userId){
        // 查询用户是否已经绑定过
        Users user = userService.findById(userId);
        if( (ObjectUtils.isEmpty(user) || (user.getIsLock().equals(1)))){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前账户已经被系统锁定。如有疑问，请联系客户！"));
        }

        if(!StringUtils.isEmpty(user.getPhone())){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前账户已经绑定手机！"));
        }

        boolean notExistsState = userService.notExistsByPhone(voAnonSmsReq.getPhone()) ;

        if(!notExistsState) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机已在平台注册！"));
        }

        MqConfig config = new MqConfig() ;
        config.setQueue(MqQueueEnum.RABBITMQ_SMS);
        config.setTag(MqTagEnum.SMS_BUNDLE);
        ImmutableMap<String, String> body = ImmutableMap
                .of(MqConfig.PHONE, voAnonSmsReq.getPhone(), MqConfig.IP, request.getRemoteAddr()) ;
        config.setMsg(body);

        boolean state = apollomqHelper.convertAndSend(config);

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
        // 2.判断用户是否存在
        Users users = userService.findById(voUserSmsReq.getUserId());

        if(ObjectUtils.isEmpty(users)){ // 判断用户是否存在
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户不存在！"));
        }

        // 3.判断用户是否绑定手机
        String mobile = users.getPhone() ;
        if(StringUtils.isEmpty(mobile)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户未绑定手机，请绑定手机！"));
        }


        // 4.请求即信发送验证码
        SmsCodeApplyRequest request = new SmsCodeApplyRequest() ;
        request.setSrvTxCode(SrvTxCodeContants.ACCOUNT_OPEN_PLUS) ;
        request.setMobile(mobile) ;
        request.setChannel(ChannelContant.HTML);
        SmsCodeApplyResponse body = jixinManager.send(
                JixinTxCodeEnum.SMS_CODE_APPLY,
                request,
                SmsCodeApplyResponse.class);

        if(ObjectUtils.isEmpty(body)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前通讯网络不稳定，请稍候重试！"));
        }

        if(!JixinResultContants.SUCCESS.equals(body.getRetCode())){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, body.getRetMsg()));
        }

        // 5.将授权码放入redis中
        try {
            redisHelper.put(
                    String.format("%s_%s", SrvTxCodeContants.ACCOUNT_OPEN_PLUS, mobile),
                    body.getSrvAuthCode(),
                    15 * 60);

        } catch (Exception e) {
            log.error("即信授权码写入redis异常", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍候重试！"));
        }

        return ResponseEntity.ok(VoBaseResp.ok("短信发送成功"));

    }
}
