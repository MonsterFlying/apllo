package com.gofobao.framework.asset.controller.web;

import com.gofobao.framework.asset.biz.CashDetailLogBiz;
import com.gofobao.framework.asset.vo.request.VoCashReq;
import com.gofobao.framework.asset.vo.request.VoPcCashLogs;
import com.gofobao.framework.asset.vo.request.VoPcRechargeReq;
import com.gofobao.framework.asset.vo.response.VoCashLogDetailResp;
import com.gofobao.framework.asset.vo.response.VoPreCashResp;
import com.gofobao.framework.asset.vo.response.pc.VoCashLogWarpRes;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
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
 * Created by admin on 2017/5/22.
 */
@Api(description = "pc:提现")
@RestController
@Slf4j
@RequestMapping("")
public class WebCashController {

    @Autowired
    private CashDetailLogBiz cashDetailLogBiz;

    @ApiOperation("pc:提现日志")
    @RequestMapping(value = "/cash/pc/v2/list",method = RequestMethod.POST)
    public ResponseEntity<VoCashLogWarpRes> list(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                  VoPcCashLogs cashLogs) {
        cashLogs.setUserId(userId);
        return cashDetailLogBiz.psLogs(cashLogs);
    }


    @ApiOperation("资金流水导出")
    @RequestMapping(value = "pub/cash/pc/v2/toExcel", method = RequestMethod.GET)
    public void pcAssetLogToExcel(HttpServletResponse response, @ModelAttribute VoPcCashLogs cashLogs,
                                  @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        cashLogs.setUserId(userId);
        cashDetailLogBiz.toExcel( cashLogs,  response);
    }



    @ApiOperation("提现")
    @PostMapping("/asset/pc/v2/cash")
    public ResponseEntity<VoHtmlResp> cash(HttpServletRequest httpServletRequest,
                                           @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                           @Valid @ModelAttribute VoCashReq voCashReq) throws Exception{
        return cashDetailLogBiz.cash(httpServletRequest, userId, voCashReq) ;
    }

    @ApiOperation("提现前期请求")
    @GetMapping("/asset/pc/v2/cash/show")
    public ResponseEntity<VoPreCashResp> preCash(HttpServletRequest httpServletRequest,
                                                 @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return cashDetailLogBiz.preCash(userId, httpServletRequest) ;
    }

    @ApiOperation("获取提现详情")
    @GetMapping("/asset/pc/v2/cash/logDetail/{id}")
    public ResponseEntity<VoCashLogDetailResp> logDetail(@PathVariable("id") Long id,
                                                         @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return cashDetailLogBiz.logDetail(id,userId) ;
    }
}
