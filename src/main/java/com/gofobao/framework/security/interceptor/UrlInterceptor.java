package com.gofobao.framework.security.interceptor;

import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/**
 * Created by admin on 2017/8/15.
 */
@Component
public class UrlInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Value("${jwt.header}")
    public  String tokenHeader;

    @Value("${jwt.prefix}")
    public String prefix;

    @Autowired
    public UserService userService;

    private Map<String, Object> errorMap = ImmutableMap.of("code", 0, "msg", "对不起,系统拒绝了您的非法访问");


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        String authToken = request.getHeader(this.tokenHeader);
        //判断token是否为空
        if (StringUtils.isEmpty(authToken)) {
            //理财||金服都通过
            return true;
        } else {
            //获取token 并验证用户
            if (!StringUtils.isEmpty(authToken) && (authToken.contains(prefix))) {
                authToken = authToken.substring(7);
            }
            Long userId = jwtTokenHelper.getUserIdFromToken(authToken);
            Users users = userService.findById(userId);
            String userType = users.getType();
            String urlPath = request.getContextPath();

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            //当前用户是理财用户
            if (!StringUtils.isEmpty(userType) && userType.equals("finance")) {
                //理财用户非法访问金服用户的url资源
                if (!urlPath.contains("finance"))
                    response.setStatus(HttpStatus.SC_BAD_REQUEST);
                    errorMap.put("time", DateHelper.dateToString(new Date()));
                    String json = new Gson().toJson(errorMap);
                    response.getWriter().write(json);
                    return false;
            } else {
                //金服用户访问 非法访问理财用户的url资源
                if (urlPath.contains("finance")) {
                    response.setStatus(HttpStatus.SC_BAD_REQUEST);
                    errorMap.put("time", DateHelper.dateToString(new Date()));
                    String json = new Gson().toJson(errorMap);
                    response.getWriter().write(json);
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
