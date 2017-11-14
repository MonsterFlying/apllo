package com.gofobao.framework.member.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.vo.request.VoRegisterReq;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * Created by Zeke on 2017/5/18.
 */
@RestController
@RequestMapping("/pub")
@Slf4j
@Api(description = "用户注册")
public class RegisterController {

    @Autowired
    private UserBiz userBiz;

    @ApiOperation("用户注册")
    @PostMapping(value = "/v2/register")
    public ResponseEntity<VoBaseResp> register(HttpServletRequest request, @Valid @ModelAttribute VoRegisterReq voRegisterReq) throws Exception {
        return userBiz.register(request, voRegisterReq);
    }
}
