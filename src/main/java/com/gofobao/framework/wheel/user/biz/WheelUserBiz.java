package com.gofobao.framework.wheel.user.biz;

import com.gofobao.framework.wheel.user.vo.repsonse.CheckTicketRes;
import com.gofobao.framework.wheel.user.vo.repsonse.RegisterRes;
import com.gofobao.framework.wheel.user.vo.request.AuthLoginReq;
import com.gofobao.framework.wheel.user.vo.request.CheckTicketReq;
import com.gofobao.framework.wheel.user.vo.request.RegisterReq;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by master on 2017/10/27.
 */
public interface WheelUserBiz {


    /**
     * 3.1注册
     *
     * @param register
     * @param request
     * @return
     */
    RegisterRes register(RegisterReq register, HttpServletRequest request);

    /**
     * 3.4 票据验证接口
     *
     * @param checkTicket
     * @return
     */
    CheckTicketRes checkTicket(CheckTicketReq checkTicket);


    /**
     * 授权登录
     * @param authLogin
     * @param response
     * @param request
     * @return
     */
    String authLogin(AuthLoginReq authLogin, HttpServletResponse response, HttpServletRequest request);


}
