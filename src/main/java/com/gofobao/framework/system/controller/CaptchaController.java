package com.gofobao.framework.system.controller;

import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.system.biz.CaptchaBiz;
import com.gofobao.framework.system.vo.VoCaptchaImageResp;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图形验证码
 * Created by Max on 17/5/18.
 */
@RestController
public class CaptchaController {

    @Autowired
    DefaultKaptcha kaptcha;

    @Autowired
    RedisHelper redisHelper;

    @Autowired
    CaptchaBiz captchaBiz ;

    @ApiOperation("获取图形验证码")
    @GetMapping("/pub/captcha")
    public ResponseEntity<VoCaptchaImageResp> captcha(){
        return captchaBiz.drawImageByAnon();
    }
}
