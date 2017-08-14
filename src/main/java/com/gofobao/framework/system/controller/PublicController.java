package com.gofobao.framework.system.controller;

import com.gofobao.framework.system.biz.DictBiz;
import com.gofobao.framework.system.vo.response.VoServiceResp;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 关于我们
 * Created by Max on 17/5/22.
 */
@RestController
public class PublicController {

    @Autowired
    DictBiz dictBiz ;


    @ApiOperation("联系客户")
    @GetMapping("/pub/service")
    public ResponseEntity<VoServiceResp> service() {
        return  dictBiz.service() ;
    }
    
}
