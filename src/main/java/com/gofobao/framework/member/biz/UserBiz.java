package com.gofobao.framework.member.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.vo.request.VoRegisterReq;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.member.vo.response.pc.UserInfoExt;
import com.gofobao.framework.security.vo.VoLoginReq;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zeke on 2017/5/19.
 */
public interface UserBiz {

    /**
     * 用户注册
     * @param request 请求
     * @param voRegisterReq 注册实体
     * @return
     */
    ResponseEntity<VoBaseResp> register(HttpServletRequest request, VoRegisterReq voRegisterReq) throws Exception;

    Users findByAccount(String account);

    ResponseEntity<VoBasicUserInfoResp> getUserInfoResp(Users user);

    /**
     * 获取用户配置详情
     * @param userId
     * @return
     */
    ResponseEntity<VoBasicUserInfoResp> userInfo(Long userId) ;

    /**
     * 用户信息
     * @param userId
     * @return
     */
    ResponseEntity<UserInfoExt>pcUserInfo(Long userId);


    /**
     * 登录
     *
     * @param httpServletRequest
     * @param response
     * @param voLoginReq
     * @return
     */
    ResponseEntity<VoBasicUserInfoResp> login(HttpServletRequest httpServletRequest, HttpServletResponse response, VoLoginReq voLoginReq);



}

