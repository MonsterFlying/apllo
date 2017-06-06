package com.gofobao.framework.member.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserPasswordBiz;
import com.gofobao.framework.member.vo.request.VoCheckFindPasswordReq;
import com.gofobao.framework.member.vo.request.VoFindPasswordReq;
import com.gofobao.framework.member.vo.request.VoModifyPasswordReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Created by admin on 2017/5/17.
 */

@RestController
public class PasswordController {

/*    @Autowired
    private PasswordRepository userPasswordSetDao;*/

    @Autowired
    private UserPasswordBiz userPasswordBiz;

    /**
     * 密码设置
     */
    @PostMapping("/pub/user/password")
    public void passwordSet(HttpServletRequest request, Integer userId) {


    }

    /**
     * 用户修改密码
     *
     * @param voModifyPasswordReq
     * @return
     */
    @ApiOperation("用户修改密码")
    @PostMapping("/user/password/modify")
    public ResponseEntity<VoBaseResp> modifyPassword(@Valid @ModelAttribute VoModifyPasswordReq voModifyPasswordReq, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voModifyPasswordReq.setUserId(userId);
        return userPasswordBiz.modifyPassword(voModifyPasswordReq);
    }

    /**
     * 用户忘记密码
     *
     * @param voFindPasswordReq
     * @return
     */
    @ApiOperation("用户忘记密码")
    @PostMapping("/pub/user/password/find/modify")
    public ResponseEntity<VoBaseResp> findPassword(@Valid @ModelAttribute VoFindPasswordReq voFindPasswordReq){
        return userPasswordBiz.findPassword(voFindPasswordReq);
    }

    /**
     * 校验用户忘记密码
     *
     * @param voCheckFindPasswordReq
     * @return
     */
    @ApiOperation("校验用户忘记密码验证码")
    @PostMapping("/pub/user/password/find/check")
    public ResponseEntity<VoBaseResp> checkFindPassword(@Valid @ModelAttribute VoCheckFindPasswordReq voCheckFindPasswordReq){
        return userPasswordBiz.checkFindPassword(voCheckFindPasswordReq);
    }
}
