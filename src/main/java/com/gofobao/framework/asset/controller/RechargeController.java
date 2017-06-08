package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.request.VoRechargeReq;
import com.gofobao.framework.asset.vo.response.VoRechargeBankInfoResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Created by Max on 17/6/7.
 */
@RestController
public class RechargeController {

    @Autowired
    AssetBiz assetBiz ;

    @ApiOperation("充值")
    @PostMapping("/asset/recharge")
    public ResponseEntity<VoHtmlResp> recharge(HttpServletRequest request, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoRechargeReq voRechargeReq){
        voRechargeReq.setUserId(userId) ;
        return assetBiz.recharge(request, voRechargeReq) ;
    }


    @ApiOperation("充值回调")
    @PostMapping("/pub/asset/recharge/callback")
    public ResponseEntity<String> rechargeCallback(HttpServletRequest request, HttpServletResponse response) throws Exception{
        return assetBiz.rechargeCallback(request, response) ;
    }



    @ApiOperation("获取转账的银行账户信息")
    @PostMapping("/asset/recharge/bankAcount")
    public ResponseEntity<VoRechargeBankInfoResp> bankAcount(HttpServletRequest request, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return assetBiz.bankAcount(userId) ;
    }

    @ApiOperation("充值记录")
    @PostMapping("/asset/recharge/log")
    public ResponseEntity<VoRechargeBankInfoResp> log(HttpServletRequest request, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId ){
        return assetBiz.bankAcount(userId) ;
    }


}
