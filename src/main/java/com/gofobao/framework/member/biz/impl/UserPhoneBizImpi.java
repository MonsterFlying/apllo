package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.core.ons.config.OnsTags;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.CaptchaHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.member.biz.UserPhoneBiz;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.request.VoBindSwitchPhone;
import com.gofobao.framework.member.vo.request.VoCheckSwitchPhone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

/**
 * Created by Zeke on 2017/5/19.
 */
public class UserPhoneBizImpi implements UserPhoneBiz {

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaHelper captchaHelper;

    /**
     * 更改手机验证
     *
     * @param voCheckSwitchPhone
     * @return
     */
    public ResponseEntity<VoBaseResp> checkSwitchPhone(VoCheckSwitchPhone voCheckSwitchPhone) {
        Long userId = voCheckSwitchPhone.getUserId();
        String phoneCaptcha = voCheckSwitchPhone.getPhoneCaptcha();

        Users users = userService.findById(userId);
        if (ObjectUtils.isEmpty(users) || ObjectUtils.isEmpty(users.getPhone())) {
            ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前登录账号不可用，详情请联系管理员！"));
        }


        boolean bool = captchaHelper.checkPhoneCaptcha(users.getPhone(), phoneCaptcha, OnsTags.SMS_SWICTH_PHONE);
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
     * @param voBindSwitchPhone
     * @return
     */
    public ResponseEntity<VoBaseResp> bindSwitchPhone(VoBindSwitchPhone voBindSwitchPhone) {
        Long userId = voBindSwitchPhone.getUserId();
        String phoneCaptcha = voBindSwitchPhone.getPhoneCaptcha();
        String newPhone = voBindSwitchPhone.getNewPhone();
        String newPhoneCaptcha = voBindSwitchPhone.getNewPhoneCaptcha();

        Users users = userService.findById(userId);
        if (ObjectUtils.isEmpty(users) || ObjectUtils.isEmpty(users.getPhone())) {
            ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前登录账号不可用，详情请联系管理员！"));
        }

        String phone = users.getPhone();
        boolean bool = captchaHelper.checkPhoneCaptcha(phone, phoneCaptcha, OnsTags.SMS_SWICTH_PHONE);
        captchaHelper.removePhoneCaptcha(phone, OnsTags.SMS_SWICTH_PHONE);//删除验证码
        if (!bool) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "旧手机短信验证码验证失败，请重试!"));
        }

        bool = captchaHelper.checkPhoneCaptcha(newPhone, newPhoneCaptcha, OnsTags.SMS_BUNDLE);
        captchaHelper.removePhoneCaptcha(newPhone, OnsTags.SMS_BUNDLE);//删除验证码
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
}
