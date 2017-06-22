package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.helper.PasswordHelper;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.helper.MacthHelper;
import com.gofobao.framework.member.biz.UserPasswordBiz;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.request.VoFindPasswordReq;
import com.gofobao.framework.member.vo.request.VoModifyPasswordReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;

/**
 * Created by Zeke on 2017/5/18.
 */
@Slf4j
@Service
public class UserPasswordBizImpl implements UserPasswordBiz {

    @Autowired
    private UserService userService;

    @Autowired
    MacthHelper macthHelper;

    @Autowired
    private RedisHelper redisHelper;

    /**
     * 用户修改密码
     *
     *
     * @param userId
     * @param voModifyPasswordReq
     * @return
     */
    public ResponseEntity<VoBaseResp> modifyPassword(Long userId, VoModifyPasswordReq voModifyPasswordReq) {
        Users users = userService.findById(userId);

        if(users.getIsLock()){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前账户处于锁定状态,如有问题请联系客户!"));
        }
        boolean bool = false;
        try {
            bool = PasswordHelper.verifyPassword(users.getPassword(), voModifyPasswordReq.getOldPassword());
        } catch (Exception e) {
            log.error("UserPasswordBizImpl modifyPassword check modifyPassword error:", e);
        }

        if (!bool) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "原始密码不正确!"));
        }

        users.setUpdatedAt(new Date());
        try {
            users.setPassword(PasswordHelper.encodingPassword(voModifyPasswordReq.getNewPassword())) ;
        } catch (Exception e) {
            log.error("UserPasswordBizImpl modifyPassword check modifyPassword error:", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "服务器开小差了，请稍后重试！"));
        }
        userService.save(users) ;
        return ResponseEntity.ok(VoBaseResp.ok("密码修改成功!"));
    }


    /**
     * 用户忘记密码
     *
     * @param voFindPasswordReq
     * @return
     */
    public ResponseEntity<VoBaseResp> findPassword(VoFindPasswordReq voFindPasswordReq) {
        // 1. 验证短信验证码
        boolean bool = macthHelper.match(MqTagEnum.SMS_RESET_PASSWORD.getValue(),
                voFindPasswordReq.getPhone(),
                voFindPasswordReq.getSmsCode());//验证找回密码验证码是否正确
        if (!bool){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"短信验证码错误/已经过期, 请重新获取短信验证码!"));
        }
        // 2. 验证手机号是否存在
        Users users = userService.findByAccount(voFindPasswordReq.getPhone());
        if(ObjectUtils.isEmpty(users)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"手机号在平台不存在!"));
        }

        // 3.更新密码
        String encoPassword = null;
        try {
            encoPassword = PasswordHelper.encodingPassword(voFindPasswordReq.getPassword());
        } catch (Exception e) {
            log.error("UserPasswordBizImpl findPassword password_reset encoding error:", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,"服务器开小差了，请稍候重试！"));
        }

        users.setPassword(encoPassword);
        users.setUpdatedAt(new Date());
        userService.save(users);
        return ResponseEntity.ok(VoBaseResp.ok("找回密码成功!"));
    }

}
