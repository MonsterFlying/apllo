
package com.gofobao.framework.asset.controller.finance;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.request.VoRechargeReq;
import com.gofobao.framework.asset.vo.response.VoAliPayRechargeInfo;
import com.gofobao.framework.asset.vo.response.VoPreRechargeResp;
import com.gofobao.framework.asset.vo.response.VoRechargeEntityWrapResp;
import com.gofobao.framework.asset.vo.response.VoUnionRechargeInfo;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(description = "充值")
public class FinanceRechargeController {

    @Autowired
    private AssetBiz assetBiz ;

    @ApiOperation("联机充值")
    @PostMapping("/asset/finance/rechargeOnline")
    public ResponseEntity<VoBaseResp> rechargeOnline(HttpServletRequest request, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoRechargeReq voRechargeReq) throws Exception{
        voRechargeReq.setUserId(userId) ;
        return assetBiz.rechargeOnline(request, voRechargeReq) ;
    }


    @ApiOperation("支付宝转账信息")
    @GetMapping("/asset/finance/recharge/alipay")
    public ResponseEntity<VoAliPayRechargeInfo> alipayRechargeInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return assetBiz.alipayBankInfo(userId) ;
    }

    @ApiOperation("银行转账")
    @GetMapping("/asset/finance/recharge/union")
    public ResponseEntity<VoUnionRechargeInfo> unionRechargeInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return assetBiz.unionBankInfo(userId) ;
    }

    @ApiOperation("充值记录")
    @GetMapping("/asset/finance/recharge/log/{pageIndex}/{pageSize}")
    public ResponseEntity<VoRechargeEntityWrapResp> log(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                        @PathVariable("pageIndex") int pageIndex,
                                                        @PathVariable("pageSize")int pageSize){
        return assetBiz.log(userId, pageIndex, pageSize) ;
    }

    @ApiOperation("充值前置条件")
    @PostMapping("/asset/finance/preRecharge")
    public ResponseEntity<VoPreRechargeResp> preRecharge(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return assetBiz.preRecharge(userId) ;
    }

}
