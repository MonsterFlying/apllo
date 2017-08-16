package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.request.VoAssetLogReq;
import com.gofobao.framework.asset.vo.response.VoViewAssetLogWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by admin on 2017/5/22.
 */
@Api(description = "资金流水")
@RestController
@Slf4j
public class AssetLogController {

    @Autowired
    private AssetBiz assetBiz;

    @ApiOperation("老版本资金流水")
    @RequestMapping(value = "/assetLog/v2/list/old",method = RequestMethod.POST)
    public ResponseEntity<VoViewAssetLogWarpRes> assetLogResList(@ModelAttribute VoAssetLogReq voAssetLogReq,
                                                                 @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voAssetLogReq.setUserId(userId);
        return assetBiz.assetLogResList(voAssetLogReq);
    }


    @ApiOperation("新版资金流水")
    @RequestMapping(value = "/assetLog/v2/list",method = RequestMethod.POST)
    public ResponseEntity<VoViewAssetLogWarpRes> newAssetLogResList(@ModelAttribute VoAssetLogReq voAssetLogReq,
                                                                 @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voAssetLogReq.setUserId(userId);
        return assetBiz.newAssetLogResList(voAssetLogReq);
    }

}
