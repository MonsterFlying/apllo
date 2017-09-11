package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.request.VoRechargeReq;
import com.gofobao.framework.asset.vo.response.VoAliPayRechargeInfo;
import com.gofobao.framework.asset.vo.response.VoPreRechargeResp;
import com.gofobao.framework.asset.vo.response.VoRechargeEntityWrapResp;
import com.gofobao.framework.asset.vo.response.VoUnionRechargeInfo;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.vo.request.VoAdminRechargeReq;
import com.gofobao.framework.tender.vo.request.VoPublishRedReq;
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
 * Created by Max on 17/6/7.
 */
@RestController
@Slf4j
public class RechargeController {

    @Autowired
    private AssetBiz assetBiz ;

    @ApiOperation("联机充值")
    @PostMapping("/asset/rechargeOnline")
    public ResponseEntity<VoBaseResp> rechargeOnline(HttpServletRequest request, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoRechargeReq voRechargeReq) throws Exception{
        voRechargeReq.setUserId(userId) ;
        return assetBiz.rechargeOnline(request, voRechargeReq) ;
    }

    @ApiOperation("充值回调")
    @PostMapping("/pub/asset/recharge/callback")
    public ResponseEntity<String> rechargeCallback(HttpServletRequest request, HttpServletResponse response) throws Exception{
        return assetBiz.rechargeCallback(request, response) ;
    }

    @ApiOperation("支付宝转账信息")
    @GetMapping("/asset/recharge/alipay")
    public ResponseEntity<VoAliPayRechargeInfo> alipayRechargeInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return assetBiz.alipayBankInfo(userId) ;
    }

    @ApiOperation("银行转账")
    @GetMapping("/asset/recharge/union")
    public ResponseEntity<VoUnionRechargeInfo> unionRechargeInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return assetBiz.unionBankInfo(userId) ;
    }

    @ApiOperation("充值记录")
    @GetMapping("/asset/recharge/log/{pageIndex}/{pageSize}")
    public ResponseEntity<VoRechargeEntityWrapResp> log(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                        @PathVariable("pageIndex") int pageIndex,
                                                        @PathVariable("pageSize")int pageSize){
        return assetBiz.log(userId, pageIndex, pageSize) ;
    }

    @ApiOperation("充值前置条件")
    @PostMapping("/asset/preRecharge")
    public ResponseEntity<VoPreRechargeResp> preRecharge(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return assetBiz.preRecharge(userId) ;
    }


    @ApiOperation("后台确认充值成功")
    @PostMapping("pub/recharge")
    public ResponseEntity<VoBaseResp> adminRechargeForm(@ModelAttribute VoAdminRechargeReq voAdminRechargeReq) {
        String paramStr = voAdminRechargeReq.getParamStr();
        if (!SecurityHelper.checkSign(voAdminRechargeReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "确认充值成功, 签名验证不通过!"));
        }

        try {
            return assetBiz.adminRechargeForm(voAdminRechargeReq) ;
        } catch (Exception e) {
            log.error("后台审核通过失败", e);
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "后台审核通过失败!"));
        }
    }

}
