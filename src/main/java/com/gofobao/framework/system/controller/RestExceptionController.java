package com.gofobao.framework.system.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.exception.LoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理接口类
 * Created by Max on 17/3/16.
 */
@RestControllerAdvice
@Slf4j
public class RestExceptionController {
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<VoBaseResp> restExceptionHandler(HttpServletRequest request, Throwable e) throws Exception {
        VoBaseResp voBaseResp = VoBaseResp.error(VoBaseResp.ERROR, e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(voBaseResp);
    }

    @ExceptionHandler(value = {LoginException.class})
    public ResponseEntity<VoBaseResp> restLoginExceptionHandler(HttpServletRequest request, Throwable e) throws Exception {
        VoBaseResp voBaseResp = VoBaseResp.error(VoBaseResp.RELOGIN, e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(voBaseResp);
    }
}
