package com.gofobao.framework.wheel.user.controller;

import com.gofobao.framework.wheel.user.biz.WheelUserBiz;
import com.gofobao.framework.wheel.user.vo.repsonse.RegisterRes;
import com.gofobao.framework.wheel.user.vo.request.RegisterReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


/**
 * Created by master on 2017/10/27.
 */
@RestController
@RequestMapping("/pub/windmill")
@Slf4j
public class WheelUserController {

    @Autowired
    private WheelUserBiz wheelUserService;

    @RequestMapping("user/register")
    public RegisterRes register(RegisterReq registerReq, HttpServletRequest request) {
        return wheelUserService.register(registerReq, request);
    }


}
