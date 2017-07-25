package com.gofobao.framework.asset.controller.web;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.biz.RechargeLogsBiz;
import com.gofobao.framework.asset.vo.request.VoPcRechargeReq;
import com.gofobao.framework.asset.vo.request.VoRechargeReq;
import com.gofobao.framework.asset.vo.response.VoPreRechargeResp;
import com.gofobao.framework.asset.vo.response.VoRechargeBankInfoResp;
import com.gofobao.framework.asset.vo.response.pc.VoViewRechargeWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Created by admin on 2017/7/3.
 */
@Api(description = "pc:充值记录")
@RestController
@Slf4j
public class WebRechargeController {


    @Autowired
    private AssetBiz assetBiz ;


    @Autowired
    private RechargeLogsBiz rechargeLogsBiz;

    @ApiOperation("pc:充值记录")
    @RequestMapping(value = "/pub/recharge/pc/v2/list",method = RequestMethod.POST)
    public ResponseEntity<VoViewRechargeWarpRes> list(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                      @ModelAttribute VoPcRechargeReq rechargeReq) {
        rechargeReq.setUserId(userId);
        return rechargeLogsBiz.logs(rechargeReq);
    }


    @ApiOperation("充值前置条件")
    @PostMapping("/asset/pc/v2/preRecharge")
    public ResponseEntity<VoPreRechargeResp> preRecharge(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return assetBiz.preRecharge(userId) ;
    }


    @ApiOperation("联机充值")
    @PostMapping("/asset/pc/v2/rechargeOnline")
    public ResponseEntity<VoBaseResp> rechargeOnline(HttpServletRequest request,
                                                     @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                     @Valid @ModelAttribute VoRechargeReq voRechargeReq) throws Exception{
        voRechargeReq.setUserId(userId) ;
        return assetBiz.rechargeOnline(request, voRechargeReq) ;
    }


    @ApiOperation("资金流水导出")
    @RequestMapping(value = "pub/recharge/pc/v2/toExcel", method = RequestMethod.GET)
    public void pcAssetLogToExcel(HttpServletResponse response, @ModelAttribute VoPcRechargeReq rechargeReq,
                                  @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        rechargeReq.setUserId(userId);
        rechargeLogsBiz.pcToExcel(rechargeReq,response);
    }


    @ApiOperation("获取转账的银行账户信息")
    @GetMapping("/asset/recharge/pc/V2/bankAccount")
    public ResponseEntity<VoRechargeBankInfoResp> bankAccount(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return assetBiz.bankAcount(userId) ;
    }

}
