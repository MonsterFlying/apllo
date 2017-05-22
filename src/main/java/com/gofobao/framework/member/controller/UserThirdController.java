package com.gofobao.framework.member.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserThirdBiz;
import com.gofobao.framework.member.vo.request.VoCheckSwitchPhoneReq;
import com.gofobao.framework.member.vo.response.VoPreOpenAccountResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 存管账户
 * Created by Max on 17/5/22.
 */
@RestController
@RequestMapping("/user/third")
public class UserThirdController {
    @Autowired
    private UserThirdBiz userThirdBiz ;


    @ApiOperation("银行存管开户前置请求")
    @PostMapping("/preOpenAccout")
    public ResponseEntity<VoPreOpenAccountResp> preOpenAccout(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return userThirdBiz.preOpenAccount(userId)  ;
    }


    @ApiOperation("银行存管开户")
    @PostMapping("/openAccout")
    public ResponseEntity<VoBaseResp> openAccount(@Valid @ModelAttribute VoCheckSwitchPhoneReq voCheckSwitchPhoneReq, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {

        return null ;
    }
}
