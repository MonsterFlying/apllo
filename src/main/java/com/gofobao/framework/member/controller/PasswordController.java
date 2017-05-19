package com.gofobao.framework.member.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserPasswordBiz;
import com.gofobao.framework.member.vo.request.VoCheckFindPassword;
import com.gofobao.framework.member.vo.request.VoFindPassword;
import com.gofobao.framework.member.vo.request.VoModifyPassword;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * @param voModifyPassword
     * @return
     */
    @ApiOperation("用户修改密码")
    @PostMapping("/user/modifyPassword")
    public ResponseEntity<VoBaseResp> modifyPassword(@Valid @ModelAttribute VoModifyPassword voModifyPassword, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voModifyPassword.setUserId(userId);
        return userPasswordBiz.modifyPassword(voModifyPassword);
    }

    /**
     * 用户忘记密码
     *
     * @param voFindPassword
     * @return
     */
    @ApiOperation("用户忘记密码")
    @PostMapping("/pub/user/findPassword")
    public ResponseEntity<VoBaseResp> findPassword(@Valid @ModelAttribute VoFindPassword voFindPassword){
        return userPasswordBiz.findPassword(voFindPassword);
    }

    /**
     * 校验用户忘记密码
     *
     * @param voCheckFindPassword
     * @return
     */
    @ApiOperation("校验用户忘记密码验证码")
    @PostMapping("/pub/user/checkFindPassword")
    public ResponseEntity<VoBaseResp> checkFindPassword(@Valid @ModelAttribute VoCheckFindPassword voCheckFindPassword){
        return userPasswordBiz.checkFindPassword(voCheckFindPassword);
    }
}
