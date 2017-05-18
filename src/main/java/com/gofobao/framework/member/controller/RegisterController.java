package com.gofobao.framework.member.controller;

import com.gofobao.framework.member.vo.request.VoRegisterReq;
import com.gofobao.framework.member.vo.response.VoRegisterCallResp;
import com.gofobao.framework.member.vo.response.VoRegisterResp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Zeke on 2017/5/18.
 */
public class RegisterController {

    /**
     * 注册用户回调
     * @param request
     * @return
     */
    public void registerCall(HttpServletRequest request){

    }

    /**
     * 注册用户
     * @param voRegisterReq
     * @return
     */
    public ResponseEntity<VoRegisterResp> register(VoRegisterReq voRegisterReq){
        return null;
    }
}
