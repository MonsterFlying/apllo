package com.gofobao.framework.wheel.user.controller;

import com.gofobao.framework.wheel.user.biz.WheelUserBiz;
import com.gofobao.framework.wheel.user.vo.repsonse.RegisterRes;
import com.gofobao.framework.wheel.user.vo.request.AuthLoginReq;
import com.gofobao.framework.wheel.user.vo.request.RegisterReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * @author master
 * @date 2017/10/27
 */
@RestController
@RequestMapping("/pub/wheel")
@Slf4j
public class WheelUserController {

    @Autowired
    private WheelUserBiz wheelUserService;

    /**
     * 注册
     *
     * @param from       来源
     * @param cl_user_id 车轮票据
     * @param mobile     手机
     * @param request
     * @return
     */
    @RequestMapping(value = "user/register")
    public RegisterRes register(@RequestParam(name = "from") String from,
                                @RequestAttribute(name = "cl_user_id") String cl_user_id,
                                @RequestAttribute(name = "mobile") String mobile,
                                HttpServletRequest request) {
        RegisterReq register = new RegisterReq();
        register.setCl_user_id(cl_user_id);
        register.setMobile(mobile);
        register.setFrom(from);
        return wheelUserService.register(register, request);
    }

    /**
     * 授权登陆
     *
     * @param from
     * @param ticket
     * @param target_url
     * @param response
     */
    @RequestMapping(value = "user/auth/login", method = RequestMethod.GET)
    public void authLogin(@RequestParam(name = "from") String from,
                          @RequestAttribute(name = "ticket") String ticket,
                          @RequestAttribute(name = "target_url") String target_url,
                          HttpServletResponse response,
                          HttpServletRequest request) {
        try {
            AuthLoginReq authLogin = new AuthLoginReq();
            authLogin.setFrom(from);
            authLogin.setTicket(ticket);
            authLogin.setTarget_url(target_url);
            response.sendRedirect(wheelUserService.authLogin(authLogin, response, request));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
