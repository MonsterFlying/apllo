package com.gofobao.framework.security.controller;

import com.gofobao.framework.member.entity.Users;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.member.service.UserService;
import com.gofobao.framework.member.vo.VoBasicUserInfo;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import com.gofobao.framework.security.vo.VoLoginReq;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<VoBasicUserInfo> login(HttpServletResponse response, @ModelAttribute VoLoginReq voLoginReq){
        // Perform the security
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        voLoginReq.getAccount(),
                        voLoginReq.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Reload password post-security so we can generate token
        Users where = new Users() ;
        where.setEmail(voLoginReq.getAccount()) ;
        where.setPhone(voLoginReq.getAccount()) ;
        where.setUsername(voLoginReq.getAccount());
        final List<Users> users = userService.listUser(where) ;

        if(CollectionUtils.isEmpty(users)){
            return ResponseEntity.badRequest().body(null);
        }

        Users user = users.get(0);
        final String token = jwtTokenHelper.generateToken(user, voLoginReq.getSource());
        response.addHeader(tokenHeader, String.format("%s %s", prefix, token));
        // Return the token
        VoBasicUserInfo voBasicUserInfo = new VoBasicUserInfo();
        voBasicUserInfo.setEmail(user.getEmail());
        voBasicUserInfo.setPhone(user.getPhone());
        return ResponseEntity.ok(voBasicUserInfo);
    }

}
