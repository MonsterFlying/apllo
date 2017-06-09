package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.asset.vo.request.VoJudgmentAvailableReq;
import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.CaptchaHelper;
import com.gofobao.framework.member.biz.UserPhoneBiz;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.request.VoBindSwitchPhoneReq;
import com.gofobao.framework.member.vo.request.VoCheckSwitchPhoneReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * Created by Zeke on 2017/5/19.
 */
@Service
public class UserPhoneBizImpi implements UserPhoneBiz {

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaHelper captchaHelper;

    /**
     * 更改手机验证
     *
     * @param voCheckSwitchPhoneReq
     * @return
     */
    public ResponseEntity<VoBaseResp> checkSwitchPhone(VoCheckSwitchPhoneReq voCheckSwitchPhoneReq) {
        Long userId = voCheckSwitchPhoneReq.getUserId();
        String phoneCaptcha = voCheckSwitchPhoneReq.getPhoneCaptcha();

        Users users = userService.findById(userId);
        if (ObjectUtils.isEmpty(users) || ObjectUtils.isEmpty(users.getPhone())) {
            ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前登录账号不可用，详情请联系管理员！"));
        }


        boolean bool = captchaHelper.checkPhoneCaptcha(users.getPhone(), phoneCaptcha, MqTagEnum.SMS_SWICTH_PHONE.getValue());
        if (!bool) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "短信验证码验证失败，请重试!"));
        }

        return ResponseEntity.ok(VoBaseResp.ok("更改手机验证成功!"));
    }

    /**
     * 更换手机绑定
     *
     * @param voBindSwitchPhoneReq
     * @return
     */
    public ResponseEntity<VoBaseResp> bindSwitchPhone(VoBindSwitchPhoneReq voBindSwitchPhoneReq) {
        Long userId = voBindSwitchPhoneReq.getUserId();
        String phoneCaptcha = voBindSwitchPhoneReq.getPhoneCaptcha();
        String newPhone = voBindSwitchPhoneReq.getNewPhone();
        String newPhoneCaptcha = voBindSwitchPhoneReq.getNewPhoneCaptcha();

        Users users = userService.findById(userId);
        if (ObjectUtils.isEmpty(users) || ObjectUtils.isEmpty(users.getPhone())) {
            ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前登录账号不可用，详情请联系管理员！"));
        }

        String phone = users.getPhone();
        boolean bool = captchaHelper.checkPhoneCaptcha(phone, phoneCaptcha, MqTagEnum.SMS_SWICTH_PHONE.getValue());
        captchaHelper.removePhoneCaptcha(phone, MqTagEnum.SMS_SWICTH_PHONE.getValue());//删除验证码
        if (!bool) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "旧手机短信验证码验证失败，请重试!"));
        }

        bool = captchaHelper.checkPhoneCaptcha(newPhone, newPhoneCaptcha, MqTagEnum.SMS_BUNDLE.getValue());
        captchaHelper.removePhoneCaptcha(newPhone, MqTagEnum.SMS_BUNDLE.getValue());//删除验证码
        if (!bool) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "新手机短信验证码验证失败，请重试!"));
        }

        Users updUsers = new Users();
        updUsers.setPhone(newPhone);
        updUsers.setId(userId);
        if (!userService.updUserById(users)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "更换手机绑定失败!"));
        }

        return ResponseEntity.ok(VoBaseResp.ok("更换手机绑定成功!"));
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
}
