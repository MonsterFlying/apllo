package com.gofobao.framework.security.controller;

import com.gofobao.framework.member.vo.VoBasicUserInfo;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.gofobao.framework.security.vo.VoLoginReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 权限验证模块
 * Created by Max on 2017/5/16.
 */
@RestController
@RequestMapping("/auth")
public class AuthenticationRestController {
    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${jwt.prefix}")
    private String prefix ;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    public ResponseEntity<?> login(HttpServletResponse response, @RequestBody VoLoginReq voLoginReq){

        // Perform the security
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        voLoginReq.getAccount(),
                        voLoginReq.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Reload password post-security so we can generate token
        final UserDetails userDetails = userDetailsService.loadUserByUsername(voLoginReq.getAccount());
        final String token = jwtTokenHelper.generateToken(userDetails, voLoginReq.getSource());
        response.addHeader(tokenHeader, String.format("%s %s", prefix, token));
        // Return the token
        return ResponseEntity.ok(new VoBasicUserInfo());
    }

}
