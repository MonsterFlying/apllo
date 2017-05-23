package com.gofobao.framework.currency.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.currency.biz.CurrencyBiz;
import com.gofobao.framework.currency.vo.request.VoListCurrencyReq;
import com.gofobao.framework.security.contants.SecurityContants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/5/23.
 */
@RestController
@RequestMapping
public class CurrencyController {

    @Autowired
    private CurrencyBiz currencyBiz;

    @PostMapping("/currency/list")
    public ResponseEntity<VoBaseResp> list(@Valid @ModelAttribute VoListCurrencyReq voListCurrencyReq, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        voListCurrencyReq.setUserId(userId);
        return currencyBiz.list(voListCurrencyReq);
    }


}
