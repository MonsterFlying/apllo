package com.gofobao.framework.integral.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.integral.biz.IntegralBiz;
import com.gofobao.framework.integral.vo.request.VoListIntegralReq;
import com.gofobao.framework.security.contants.SecurityContants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/5/22.
 */
@RestController
@RequestMapping
public class IntegralController {

    @Autowired
    private IntegralBiz integralBiz;

    /**
     * 获取积分列表
     *
     * @param voListIntegralReq
     * @return
     */
    public ResponseEntity<VoBaseResp> list(@Valid @ModelAttribute VoListIntegralReq voListIntegralReq, @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        voListIntegralReq.setUserId(userId);
        return integralBiz.list(voListIntegralReq);
    }
}
