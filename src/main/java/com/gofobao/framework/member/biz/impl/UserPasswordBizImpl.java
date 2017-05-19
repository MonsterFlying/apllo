package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.core.helper.PasswordHelper;
import com.gofobao.framework.core.ons.config.OnsTags;
import com.gofobao.framework.core.vo.VoBaseResp;
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
    private RedisHelper redisHelper;

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
            bool = PasswordHelper.verifyPassword(nowPassword,users.getPassword());
        } catch (Exception e) {
            log.error("UserPasswordBizImpl modifyPassword check password error:",e);
        }

        if (!bool){
            ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"当前密码验证不通过!"));
        }

        Users saveUsers = new Users();
        saveUsers.setId(users.getId());
        saveUsers.setPassword(newPassword);
        if (!userService.updUserByPhone(saveUsers)){
            ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"用户密码修改失败!"));
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

        ResponseEntity responseEntity = checkPhoneCaptcha(phone, phoneCaptcha,OnsTags.SMS_RESET_PASSWORD);//验证找回密码验证码是否正确
        if (!ObjectUtils.isEmpty(responseEntity)) {
            return responseEntity;
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

        ResponseEntity responseEntity = checkPhoneCaptcha(phone, phoneCaptcha,OnsTags.SMS_RESET_PASSWORD);//验证找回密码验证码是否正确
        if (!ObjectUtils.isEmpty(responseEntity)) {
            return responseEntity;
        }

        String encoPassword = null;
        try {
            encoPassword = PasswordHelper.encodingPassword(newPassword);
        } catch (Exception e) {
            log.error("UserPasswordBizImpl findPassword password encoding error:",e);
        }

        Users users = new Users();
        users.setPhone(phone);
        users.setPassword(encoPassword);
        if (!userService.updUserByPhone(users)){
            ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"用户密码修改失败!"));
        }

        return ResponseEntity.ok(VoBaseResp.ok("用户密码修改成功!"));
    }

    /**
     * 校验忘记密码手机验证码
     * @param phone
     * @param phoneCaptcha
     * @param SMSType
     * @return
     */
    private ResponseEntity<VoBaseResp> checkPhoneCaptcha(String phone, String phoneCaptcha,String SMSType) {
        String checkPhoneCaptcha = null;
        try {
            checkPhoneCaptcha = redisHelper.get(String.format("%s_%s", SMSType, phone), "");
        } catch (Exception e) {
            log.error("UserPasswordBizImpl findPassword phoneCaptcha not exist:", e);
        }

        if (StringUtils.isEmpty(checkPhoneCaptcha)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "手机验证码失效，请重新获取!"));
        }

        if (checkPhoneCaptcha.equals(phoneCaptcha)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "手机验证码输入错误，请重新输入!"));
        }

        return null;
    }
}
