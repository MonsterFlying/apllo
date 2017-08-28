package com.gofobao.framework.system.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.exception.LoginException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 全局异常处理接口类
 * Created by Max on 17/3/16.
 */
@RestControllerAdvice
@Slf4j
public class RestExceptionController {
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<VoBaseResp> restExceptionHandler(HttpServletRequest request, Throwable e) throws Exception {
        VoBaseResp voBaseResp = VoBaseResp.error(VoBaseResp.ERROR, "系统系统, 请稍后重试!");
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



    /**
     * 处理请求参数验证异常
     *
     * @param ex
     * @return
     * @throws Exception
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public ResponseEntity<VoBaseResp> processValidationError(BindException ex) throws Exception {
        BindingResult result = ex.getBindingResult();
        List<ObjectError> allErrors = result.getAllErrors();
        for (ObjectError error : allErrors) {
            return new ResponseEntity(VoBaseResp.error(VoBaseResp.ERROR, error.getDefaultMessage()), HttpStatus.BAD_REQUEST) ;
        }

        return new ResponseEntity(VoBaseResp.error(VoBaseResp.ERROR, "系统异常, 请稍后重试!"), HttpStatus.BAD_REQUEST) ;
    }
}
