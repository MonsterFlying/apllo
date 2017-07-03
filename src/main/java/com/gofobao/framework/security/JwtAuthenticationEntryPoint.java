package com.gofobao.framework.security;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    Gson GSON = new Gson() ;

    private static final long serialVersionUID = -8970718410437077606L;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // This is invoked when user tries to access a secured REST resource without supplying any credentials
        // We should just send a 401 Unauthorized response because there is no 'login page' to redirect to
        try(PrintWriter printWriter = response.getWriter()) {
            VoBaseResp error = VoBaseResp.error(VoBaseResp.RELOGIN,"该操作需要登录才能进行!");
            printWriter.write(GSON.toJson(error));
            printWriter.flush();
        }catch (Exception ex){
            log.error("非法访问") ;
        }
    }
}