package com.gofobao.framework.windmill.user.biz;

import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.windmill.user.vo.request.BindLoginReq;
import com.gofobao.framework.windmill.user.vo.respones.UserRegisterRes;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by admin on 2017/7/31.
 */
public interface WindmillUserBiz {


    /**
     *
     * @param request
     * @return
     */
    UserRegisterRes register(HttpServletRequest request) ;


    /**
     *
     * @param request
     * @param response
     * @param bindLoginReq
     * @return
     */
   ResponseEntity<VoBasicUserInfoResp> bindLogin(HttpServletRequest request, HttpServletResponse response, BindLoginReq bindLoginReq);

    /**
     * 用户登录
     * @param request
     * @return
     */
   String login(HttpServletRequest request, HttpServletResponse response);




}
