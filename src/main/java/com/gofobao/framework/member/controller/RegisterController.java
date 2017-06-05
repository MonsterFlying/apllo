package com.gofobao.framework.member.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.request.VoRegisterReq;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Created by Zeke on 2017/5/18.
 */
@RestController
@RequestMapping("/pub")
@Slf4j
@ApiModel("用户注册")
public class RegisterController {

    @Autowired
    private UserBiz userBiz;

    @ApiOperation("用户注册")
    @PostMapping(value = "/v2/register")
    public ResponseEntity<VoBaseResp> register(HttpServletRequest request, @Valid @ModelAttribute VoRegisterReq voRegisterReq) throws Exception{
        return userBiz.register(request, voRegisterReq) ;
    }
}
