package com.gofobao.framework.currency.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.currency.biz.CurrencyBiz;
import com.gofobao.framework.currency.vo.request.VoListCurrencyReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/5/23.
 */
@RestController
@RequestMapping
public class CurrencyController {

    @Autowired
    private CurrencyBiz currencyBiz;

    public ResponseEntity<VoBaseResp> list(@Valid @ModelAttribute VoListCurrencyReq voListCurrencyReq){
        return null;
    }
}
