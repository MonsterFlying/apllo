package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.response.VoUserAssetInfoResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Zeke on 2017/5/19.
 */
@Api(description = "资金模块")
@RestController
public class AssetController {

    @Autowired
    private AssetBiz assetBiz;

    @ApiOperation("获取用户资产信息")
    @GetMapping("/asset/v2/info")
    public ResponseEntity<VoUserAssetInfoResp> userAssetInfo(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.userAssetInfo(userId);
    }


    @GetMapping("/asset/v2/synAsset/{time}")
    public ResponseEntity<VoBaseResp> synAsset(@RequestAttribute(SecurityContants.USERID_KEY) Long userId, @PathVariable("time") String time) {
        return assetBiz.synchronizedAsset(userId, time) ;
    }

}
