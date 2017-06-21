package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.BankAccountBiz;
import com.gofobao.framework.asset.vo.response.VoBankListResp;
import com.gofobao.framework.asset.vo.response.VoBankTypeInfoResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by Zeke on 2017/5/22.
 */
@RestController
public class BankAccountController {

    @Autowired
    private BankAccountBiz bankAccountBiz ;

    @GetMapping("/bank/typeinfo/{account}")
    @ApiOperation("根据银行卡获取银行卡基础信息和限额")
    public ResponseEntity<VoBankTypeInfoResp> findTypeInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @PathVariable("account") String account){
        return bankAccountBiz.findTypeInfo(userId, account) ;
    }


    @GetMapping("/bank/credit")
    @ApiOperation("额度列表")
    public ResponseEntity<VoHtmlResp> credit(){
        return bankAccountBiz.credit() ;
    }



    @GetMapping("/bank/list")
    @ApiOperation("额度列表")
    public ResponseEntity<VoBankListResp> list(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return bankAccountBiz.list(userId) ;
    }

}
