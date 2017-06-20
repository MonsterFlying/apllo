package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.request.VoAssetLogReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sun.plugin2.message.helper.URLHelper;

/**
 * Created by admin on 2017/5/22.
 */
@Api(description = "资金流水")
@RestController
@RequestMapping("/assetLog")
@Slf4j
public class AssetLogController {

    @Autowired
    private AssetBiz assetBiz;

    @RequestMapping(value = "/v2/list",method = RequestMethod.POST)
    public ResponseEntity assetLogResList(@ModelAttribute VoAssetLogReq voAssetLogReq, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voAssetLogReq.setUserId(userId);
        return assetBiz.assetLogResList(voAssetLogReq);
    }


}
