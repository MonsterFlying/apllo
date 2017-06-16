package com.gofobao.framework.member.biz.impl;

import com.gofobao.framework.common.rabbitmq.MqTagEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.MacthHelper;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.biz.UserEmailBiz;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.member.vo.request.VoBindEmailReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * Created by Administrator on 2017/6/16 0016.
 */
@Component
public class UserEmailBizImpl implements UserEmailBiz {

    @Autowired
    UserBiz userBiz ;

    @Autowired
    UserService userService ;

    @Autowired
    MacthHelper macthHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<VoBasicUserInfoResp> bindEmail(VoBindEmailReq voBindEmailReq, Long userId) {
        // 验证用户是否无效
        Users user = userService.findByIdLock(userId);
        if(ObjectUtils.isEmpty(user)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户不存在！", VoBasicUserInfoResp.class));
        }

        // 验证用户是否已经绑定邮箱
        if(!StringUtils.isEmpty(user.getEmail())){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前用户已经绑定邮箱,请勿重新绑定!", VoBasicUserInfoResp.class));
        }

        // 邮箱邮箱是否唯一
        boolean notExistsState = userService.notExistsByEmail(voBindEmailReq.getEmail());
        if(!notExistsState){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "当前邮箱已在平台注册,请选择其他邮箱注册!", VoBasicUserInfoResp.class));
        }

        // 验证邮箱
        boolean match = macthHelper.match(MqTagEnum.SMS_EMAIL_BIND.getValue(), voBindEmailReq.getEmail(), voBindEmailReq.getEmail());
        if(!match){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "邮箱验证码错误/已过期,请重新发送邮箱验证码!", VoBasicUserInfoResp.class));
        }

        // 保存信息
        user.setEmail(voBindEmailReq.getEmail());
        user.setUpdatedAt(new Date());
        userService.save(user) ;

        return userBiz.getUserInfoResp(user) ;
    }
}
