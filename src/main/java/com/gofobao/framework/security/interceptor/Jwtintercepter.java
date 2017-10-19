package com.gofobao.framework.security.interceptor;

import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.IpHelper;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.security.exception.LoginException;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Slf4j
public class Jwtintercepter extends HandlerInterceptorAdapter {

    private JwtTokenHelper jwtTokenHelper;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

        String requestSource = httpServletRequest.getHeader("requestSource");
        try {
            if (StringUtils.isEmpty(requestSource)) {
                log.error("当前用户非法请求 记录当前ip:" + IpHelper.getIpAddress(httpServletRequest) + ",当前时间:" + DateHelper.dateToString(new Date()));
            }
        } catch (Exception e) {

        }

        // 判断当前用户路劲
        String url = httpServletRequest.getRequestURI();

        if (url.contains("version")) {
            return true;
        }

        if (ObjectUtils.isEmpty(jwtTokenHelper)) {  // 初始化 jwtTokenHelper
            ApplicationContext ac = WebApplicationContextUtils.getRequiredWebApplicationContext(httpServletRequest.getServletContext());
            jwtTokenHelper = (JwtTokenHelper) ac.getBean("jwtTokenHelper");
        }

        String token = jwtTokenHelper.getToken(httpServletRequest);
        if (StringUtils.isEmpty(token)) {
            return false;
        }

        try {
            jwtTokenHelper.validateSign(token);
        } catch (Exception e) {
            throw new LoginException(e.getMessage());
        }

        Long userId = jwtTokenHelper.getUserIdFromToken(token);  // 用户ID
        httpServletRequest.setAttribute(SecurityContants.USERID_KEY, userId);
        try {
            String requestSourceStr = StringUtils.isEmpty(requestSource) ? "未知来源" : requestSource;
            log.info(String.format("当前请求地址：%s，来源: %s , 终端ip: %s", url, requestSourceStr, httpServletRequest.getRemoteAddr()));
        } catch (Exception e) {
        }
        String type = jwtTokenHelper.getType(token);
        if (url.contains("/financeserver/")) { //金服理财用户
            if ("finance".equals(type)) {
                throw new Exception("系统拒绝当前请求");
            }
        } else if (url.contains("/finance/")) {  // 理财用户
            if (!"finance".equals(type)) {
                throw new Exception("系统拒绝当前请求");
            }
        } else {  // 金服用户
            if ("finance".equals(type)) {
                throw new Exception("系统拒绝当前请求");
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
