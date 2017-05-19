package com.gofobao.framework.member.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserPhoneBiz;
import com.gofobao.framework.member.vo.request.VoBindSwitchPhone;
import com.gofobao.framework.member.vo.request.VoCheckSwitchPhone;
import com.gofobao.framework.member.vo.request.VoFindPassword;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<VoBaseResp> checkSwitchPhone(@Valid @ModelAttribute VoCheckSwitchPhone voCheckSwitchPhone){
        return userPhoneBiz.checkSwitchPhone(voCheckSwitchPhone);
    }

    @ApiOperation("更改手机绑定")
    @PostMapping("/user/phone/switch/bind")
    public ResponseEntity<VoBaseResp> bindSwitchPhone(@Valid @ModelAttribute VoBindSwitchPhone voBindSwitchPhone){
        return userPhoneBiz.bindSwitchPhone(voBindSwitchPhone);
    }
}
