package com.gofobao.framework.security.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.biz.UserBiz;
import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.gofobao.framework.security.vo.VoLoginReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 权限验证模块
 * Created by Max on 2017/5/16.
 */
@RestController
@RequestMapping("/pub/auth")
public class AuthenticationRestController {
    @Value("${jwt.header}")
    String tokenHeader;

    @Value("${jwt.prefix}")
    String prefix ;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenHelper jwtTokenHelper;

    @Autowired
    UserService userService;

    @Autowired
    UserBiz userBiz ;

    @PostMapping("/login")
    public ResponseEntity<VoBasicUserInfoResp> login(HttpServletResponse response, @ModelAttribute VoLoginReq voLoginReq){
        // Perform the security
        final Authentication authentication ;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            voLoginReq.getAccount(),
                            voLoginReq.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "账户/密码错误", VoBasicUserInfoResp.class)) ;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Reload password_reset post-security so we can generate captchaToken
        final Users user = userBiz.findByAccount(voLoginReq.getAccount());

        if(ObjectUtils.isEmpty(user)){
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "账户/密码错误", VoBasicUserInfoResp.class));
        }


        if(user.getIsLock()) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "账户已被系统冻结，如有问题请联系客服！", VoBasicUserInfoResp.class));
        }

        String username = user.getUsername();
        if(StringUtils.isEmpty(username)) username = user.getPhone() ;
        if(StringUtils.isEmpty(username)) username = user.getEmail() ;
        user.setUsername(username);

        final String token = jwtTokenHelper.generateToken(user, voLoginReq.getSource());
        response.addHeader(tokenHeader, String.format("%s %s", prefix, token));
        return userBiz.getUserInfoResp(user) ;
    }

}
