package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.asset.vo.request.VoJudgmentAvailableReq;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.MacthHelper;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.biz.UserPhoneBiz;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.request.VoBindPhone;
import com.gofobao.framework.member.vo.request.VoBindSwitchPhoneReq;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * Created by Zeke on 2017/5/19.
 */
@Service
public class UserPhoneBizImpi implements UserPhoneBiz {

    @Autowired
    UserService userService;

    @Autowired
    MacthHelper macthHelper;

    @Autowired
    UserBiz userBiz;


    /**
     * 更换手机绑定
     *
     * @param voBindSwitchPhoneReq
     * @return
     */
    public ResponseEntity<VoBasicUserInfoResp> bindSwitchPhone(VoBindSwitchPhoneReq voBindSwitchPhoneReq) {
        Long userId = voBindSwitchPhoneReq.getUserId();
        String newPhone = voBindSwitchPhoneReq.getNewPhone();


        Users users = userService.findById(userId);
        if (ObjectUtils.isEmpty(users) || ObjectUtils.isEmpty(users.getPhone())) {
            ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前登录账号不可用，详情请联系管理员！", VoBasicUserInfoResp.class));
        }
        // 验证新手机是否
        boolean noExists = userService.notExistsByPhone(newPhone);
        if(!noExists){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "新手机号已在平台注册,请使用其他手机号!", VoBasicUserInfoResp.class));
        }

        String phone = users.getPhone();
        boolean bool =  macthHelper.match( MqTagEnum.SMS_SWICTH_PHONE.getValue(), phone, voBindSwitchPhoneReq.getNewPhoneCaptcha()) ;
        if (!bool) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "原手机验证码错误/或者已过期，请重新发送短信验证码!", VoBasicUserInfoResp.class));
        }

        bool =  macthHelper.match( MqTagEnum.SMS_BUNDLE.getValue(), voBindSwitchPhoneReq.getNewPhone(), voBindSwitchPhoneReq.getNewPhoneCaptcha()) ;
        if (!bool) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "新手机验证码错误/或者已过期，请重新发送短信验证码!", VoBasicUserInfoResp.class));
        }

        users.setPhone(voBindSwitchPhoneReq.getNewPhone()) ;
        users.setUpdatedAt(new Date()) ;
        userService.save(users) ;
        return userBiz.getUserInfoResp(users) ;
    }

    @Override
    public ResponseEntity<VoBaseResp> checkOnlyForUserInfo(VoJudgmentAvailableReq voJudgmentAvailableReq) {
        String msg = "查询成功" ;
        boolean flag = false ;

        if("1".equalsIgnoreCase(voJudgmentAvailableReq.getCheckType())){
            flag = !userService.notExistsByPhone(voJudgmentAvailableReq.getCheckValue()) ;
            if(flag){
                msg = "手机号已在平台注册！" ;
            }
        }else if("2".equalsIgnoreCase(voJudgmentAvailableReq.getCheckType())){
            flag = !userService.notExistsByEmail(voJudgmentAvailableReq.getCheckValue()) ;
            if(flag){
                msg = "邮箱已在平台注册！" ;
            }
        }else{
            flag = !userService.notExistsByUserName(voJudgmentAvailableReq.getCheckValue()) ;
            if(flag){
                msg = "用户名已在平台注册！" ;
            }
        }

        if(flag){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, msg));
        }else{
            return ResponseEntity.ok(VoBaseResp.ok(msg));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBasicUserInfoResp> bindPhone(VoBindPhone voBindPhone, Long userId) {
        // 判断账户手机号是否绑定状态
        Users users = userService.findByIdLock(userId);
        if(ObjectUtils.isEmpty(users)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户不存在!", VoBasicUserInfoResp.class));
        }

        if(users.getIsLock()){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户处于锁定状态!", VoBasicUserInfoResp.class));
        }

        if(!StringUtils.isEmpty(users.getPhone())){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, " 账户已经绑定手机!", VoBasicUserInfoResp.class));
        }

        // 验证手机号唯一性
        boolean noExists = userService.notExistsByPhone(voBindPhone.getPhone());
        if(!noExists){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前手机已在平台注册!", VoBasicUserInfoResp.class));
        }

        // 验证短信验证码
        boolean bool = macthHelper.match( MqTagEnum.SMS_BUNDLE.getValue(), voBindPhone.getPhone(), voBindPhone.getSmsCode()) ;
        if (!bool) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, " 短信验证码错误或者过期,请重新获取短信验证码!", VoBasicUserInfoResp.class));
        }

        // 保存手机号
        users.setPhone(voBindPhone.getPhone());
        users.setUpdatedAt(new Date());
        userService.save(users) ;
        return userBiz.getUserInfoResp(users) ;
    }
}
