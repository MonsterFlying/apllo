package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.CashDetailLogBiz;
import com.gofobao.framework.asset.vo.request.VoAdminCashReq;
import com.gofobao.framework.asset.vo.request.VoBankApsReq;
import com.gofobao.framework.asset.vo.request.VoCashReq;
import com.gofobao.framework.asset.vo.response.VoBankApsWrapResp;
import com.gofobao.framework.asset.vo.response.VoCashLogDetailResp;
import com.gofobao.framework.asset.vo.response.VoCashLogWrapResp;
import com.gofobao.framework.asset.vo.response.VoPreCashResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 *
 * Created by Max on 17/6/8.
 */
@RestController
@Api(value = "提现")
public class CashController {

    @Autowired
    private CashDetailLogBiz cashDetailLogBiz;


    @ApiOperation("提现前期请求")
    @GetMapping("/asset/cash/show")
    public ResponseEntity<VoPreCashResp> preCash(HttpServletRequest httpServletRequest,  @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return cashDetailLogBiz.preCash(userId, httpServletRequest) ;
    }


    @ApiOperation("提现")
    @PostMapping("/asset/cash")
    public ResponseEntity<VoHtmlResp> cash(HttpServletRequest httpServletRequest,  @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @Valid @ModelAttribute VoCashReq voCashReq) throws Exception{
        return cashDetailLogBiz.cash(httpServletRequest, userId, voCashReq) ;
    }

    @ApiOperation("企业后台提现")
    @PostMapping("/pub/asset/cash")
    public ResponseEntity<VoHtmlResp> adminWebCash(HttpServletRequest httpServletRequest, @Valid @ModelAttribute VoAdminCashReq voAdminCashReq) throws Exception{
        return cashDetailLogBiz.adminWebCash(httpServletRequest, voAdminCashReq) ;
    }

    @PostMapping("/pub/asset/cash/callback")
    public ResponseEntity<String> cashCallback(HttpServletRequest request) throws Exception{
        return cashDetailLogBiz.cashCallback(request) ;
    }

    @ApiOperation("联行号搜索接口")
    @PostMapping("/asset/cash/bankAps")
    public ResponseEntity<VoBankApsWrapResp> bankAps(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @ModelAttribute VoBankApsReq voBankApsReq){
        return cashDetailLogBiz.bankAps(userId, voBankApsReq) ;
    }

    @ApiOperation("提现记录")
    @GetMapping("/asset/cash/log/{pageIndex}/{pageSize}")
    public ResponseEntity<VoCashLogWrapResp> log(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId, @PathVariable("pageIndex") int pageIndex, @PathVariable("pageSize") int pageSize ){
        --pageIndex;
        return cashDetailLogBiz.log(userId, pageIndex, pageSize) ;
    }

    @ApiOperation("获取提现详情")
    @GetMapping("/asset/cash/logDetail/{id}")
    public ResponseEntity<VoCashLogDetailResp> logDetail(@PathVariable("id") Long id ){
        return cashDetailLogBiz.logDetail(id) ;
    }
}
