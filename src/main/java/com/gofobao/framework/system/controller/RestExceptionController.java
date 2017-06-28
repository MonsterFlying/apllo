package com.gofobao.framework.system.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
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
    public ResponseEntity<VoBaseResp> restExceptionHandler(HttpServletRequest request, Exception e) throws Exception {
        VoBaseResp voBaseResp = VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了, 麻烦轻声提醒一下客户!" ) ;
        log.error("全局参数检测异常", e);
        return ResponseEntity
                .badRequest()
                .body(voBaseResp);
    }

}
