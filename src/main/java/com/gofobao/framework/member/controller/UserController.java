package com.gofobao.framework.member.controller;

import com.gofobao.framework.asset.vo.request.VoJudgmentAvailableReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserPhoneBiz;
import com.gofobao.framework.member.vo.request.VoBindSwitchPhoneReq;
import com.gofobao.framework.member.vo.request.VoCheckSwitchPhoneReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by Max on 17/5/16.
 */
@RestController
@RequestMapping
public class UserController {

    @Autowired
    private UserPhoneBiz userPhoneBiz;

    @ApiOperation("更改手机验证")
    @PostMapping("/user/phone/switch/check")
    public ResponseEntity<VoBaseResp> checkSwitchPhone(@Valid @ModelAttribute VoCheckSwitchPhoneReq voCheckSwitchPhoneReq, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voCheckSwitchPhoneReq.setUserId(userId);
        return userPhoneBiz.checkSwitchPhone(voCheckSwitchPhoneReq);
    }

    @ApiOperation("更改手机绑定")
    @PostMapping("/user/phone/switch/bind")
    public ResponseEntity<VoBaseResp> bindSwitchPhone(@Valid @ModelAttribute VoBindSwitchPhoneReq voBindSwitchPhoneReq, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voBindSwitchPhoneReq.setUserId(userId);
        return userPhoneBiz.bindSwitchPhone(voBindSwitchPhoneReq);
    }

    @ApiOperation("判断账户是否可用")
    @PostMapping("/pub/user/info/checkOnly")
    public ResponseEntity<VoBaseResp> checkOnlyForUserInfo(@Valid @ModelAttribute VoJudgmentAvailableReq VoJudgmentAvailableReq) {
        return userPhoneBiz.checkOnlyForUserInfo(VoJudgmentAvailableReq);
    }
}
