package com.gofobao.framework.member.controller;

import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.request.VoRegisterReq;
import com.gofobao.framework.member.vo.response.VoRegisterCallResp;
import com.gofobao.framework.member.vo.response.VoRegisterResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Zeke on 2017/5/18.
 */
@RestController
@RequestMapping("/pub/user/reg")
public class RegisterController {

    @Autowired
    private UserService userService;

    /**
     * 注册用户回调
     * @param request
     * @return
     */
    @RequestMapping(value = "/registerCallBack")
    public void registerCallBack(HttpServletRequest request, HttpServletResponse response){
        try {
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print("访问接口:registerCallBack");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 注册用户
     * @param voRegisterReq
     * @return
     */
    @GetMapping(value = "/register")
    public ResponseEntity<VoRegisterResp> register(VoRegisterReq voRegisterReq){
        voRegisterReq.setChannel(ChannelContant.APP);
        voRegisterReq.setCardNo("6226628812120004");
        voRegisterReq.setCardId("310114198407240819");
        voRegisterReq.setUsername("卜唯渊");
        voRegisterReq.setMobile("18964826795");
        userService.register(voRegisterReq);
        return null;
    }
}
