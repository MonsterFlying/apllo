package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.response.VoPreCashResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import springfox.documentation.annotations.ApiIgnore;

/**
 *
 * Created by Max on 17/6/8.
 */
public class CashController {


    @Autowired
    AssetBiz assetBiz ;


    @ApiOperation("提现前期请求")
    @PostMapping("/asset/recharge")
    public ResponseEntity<VoPreCashResp> preCash(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return assetBiz.preCash(userId) ;
    }
}
