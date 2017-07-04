package com.gofobao.framework.asset.controller.web;

import com.gofobao.framework.asset.biz.RechargeLogsBiz;
import com.gofobao.framework.asset.vo.request.VoPcCashLogs;
import com.gofobao.framework.asset.vo.request.VoPcRechargeReq;
import com.gofobao.framework.asset.vo.response.pc.VoCashLogWarpRes;
import com.gofobao.framework.asset.vo.response.pc.VoViewRechargeWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by admin on 2017/7/3.
 */
@Api(description = "pc:充值记录")
@RestController
@Slf4j
public class WebRechargeController {

    @Autowired
    private RechargeLogsBiz rechargeLogsBiz;

    @ApiOperation("pc:提现日志")
    @RequestMapping(value = "recharge/pc/v2/list",method = RequestMethod.POST)
    public ResponseEntity<VoViewRechargeWarpRes> list(/*@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,*/
                                                       VoPcRechargeReq rechargeReq) {
        rechargeReq.setUserId(901L);
        return rechargeLogsBiz.logs(rechargeReq);
    }
    
}
