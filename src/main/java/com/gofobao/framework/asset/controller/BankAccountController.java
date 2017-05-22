package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.vo.request.VoUserBankListReq;
import com.gofobao.framework.asset.vo.response.VoUserBankListResp;
import com.gofobao.framework.security.contants.SecurityContants;
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
public class BankAccountController {

    public ResponseEntity<VoUserBankListResp> listUserBank(@Valid @ModelAttribute VoUserBankListReq voUserBankListReq, @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return null;
    }
}
