package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.core.helper.PasswordHelper;
import com.gofobao.framework.core.ons.config.OnsTags;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.CaptchaHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.member.biz.UserPasswordBiz;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.request.VoCheckFindPassword;
import com.gofobao.framework.member.vo.request.VoFindPassword;
import com.gofobao.framework.member.vo.request.VoModifyPassword;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Created by Zeke on 2017/5/18.
 */
@Slf4j
public class UserPasswordBizImpl implements UserPasswordBiz {

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaHelper captchaHelper;

    /**
     * 用户修改密码
     *
     * @param voModifyPassword
     * @return
     */
    public ResponseEntity<VoBaseResp> modifyPassword(VoModifyPassword voModifyPassword) {
        String nowPassword = voModifyPassword.getNowPassword();
        String newPassword = voModifyPassword.getNewPassword();
        Long userId = voModifyPassword.getUserId();

        Users users = userService.findById(userId);

        boolean bool = false;
        try {
            bool = PasswordHelper.verifyPassword(nowPassword, users.getPassword());
        } catch (Exception e) {
            log.error("UserPasswordBizImpl modifyPassword check password error:", e);
        }

        if (!bool) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前密码验证不通过!"));
        }

        Users updUsers = new Users();
        updUsers.setId(users.getId());
        updUsers.setPassword(newPassword);
        if (!userService.updUserByPhone(updUsers)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "用户密码修改失败!"));
        }
        return ResponseEntity.ok(VoBaseResp.ok("用户忘记密码修改成功!"));
    }

    /**
     * 校验用户忘记密码
     *
     * @param voCheckFindPassword
     * @return
     */
    public ResponseEntity<VoBaseResp> checkFindPassword(VoCheckFindPassword voCheckFindPassword) {
        String phone = voCheckFindPassword.getPhone();
        String phoneCaptcha = voCheckFindPassword.getPhoneCaptcha();

        boolean bool = captchaHelper.checkPhoneCaptcha(phone, phoneCaptcha, OnsTags.SMS_RESET_PASSWORD);//验证找回密码验证码是否正确
        if (!bool){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"用户忘记密码验证失败,请重试!"));
        }

        return ResponseEntity.ok(VoBaseResp.ok("用户忘记密码验证成功!"));
    }

    /**
     * 用户忘记密码
     *
     * @param voFindPassword
     * @return
     */
    public ResponseEntity<VoBaseResp> findPassword(VoFindPassword voFindPassword) {
        String phone = voFindPassword.getPhone();
        String phoneCaptcha = voFindPassword.getPhoneCaptcha();
        String newPassword = voFindPassword.getNewPassword();

        boolean bool = captchaHelper.checkPhoneCaptcha(phone, phoneCaptcha, OnsTags.SMS_RESET_PASSWORD);//验证找回密码验证码是否正确
        captchaHelper.removePhoneCaptcha(phone, OnsTags.SMS_RESET_PASSWORD);//删除验证码
        if (!bool){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"用户忘记密码验证失败,请重试!"));
        }

        String encoPassword = null;
        try {
            encoPassword = PasswordHelper.encodingPassword(newPassword);
        } catch (Exception e) {
            log.error("UserPasswordBizImpl findPassword password encoding error:", e);
        }

        Users users = new Users();
        users.setPhone(phone);
        users.setPassword(encoPassword);
        if (!userService.updUserByPhone(users)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "用户密码修改失败!"));
        }

        return ResponseEntity.ok(VoBaseResp.ok("用户密码修改成功!"));
    }

}
