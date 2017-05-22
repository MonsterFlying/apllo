package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.response.VoUserAssetInfoResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Zeke on 2017/5/19.
 */
@RequestMapping
@RestController
public class AssetController {

    @Autowired
    private AssetBiz assetBiz;

    @ApiOperation("获取用户资产信息")
    @PostMapping("/asset/info")
    public ResponseEntity<VoUserAssetInfoResp> userAssetInfo(@RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.userAssetInfo(userId);
    }
}
