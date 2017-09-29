package com.gofobao.framework.starfire.user.biz;

import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.starfire.user.vo.request.*;
import com.gofobao.framework.starfire.user.vo.response.FetchLoginTokenRes;
import com.gofobao.framework.starfire.user.vo.response.RegisterQueryRes;
import com.gofobao.framework.starfire.user.vo.response.RegisterRes;
import com.gofobao.framework.starfire.user.vo.response.UserAccountRes;
import com.gofobao.framework.windmill.user.vo.request.BindLoginReq;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by master on 2017/9/26.
 */
public interface StarFireUserBiz {

    /**
     * 注册绑定查询
     *
     * @param registerQuery
     * @return
     */
    RegisterQueryRes registerQuery(RegisterQuery registerQuery);

    /**
     * 2.新用户注册接口
     *
     * @param registerModel
     * @return
     */
    RegisterRes register(RegisterModel registerModel, HttpServletRequest request);

    /**
     * 用户登陆绑定
     *
     * @return
     */
    ResponseEntity<VoBasicUserInfoResp> bindLogin(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  BindLoginReq bindLoginReq);

    /**
     *获取login_token
     * @param fetchLoginToken
     * @return
     */
    FetchLoginTokenRes fetchLoginToken(FetchLoginToken fetchLoginToken);

    /**
     * 授权登录接口
     * @param loginModel
     * @return
     */
    String requestUrl(LoginModel loginModel,HttpServletRequest request,HttpServletResponse response);

    /**
     *账户信息查询
     * @param userAccount
     * @return
     */
    UserAccountRes userAccount(UserAccount userAccount);






}
