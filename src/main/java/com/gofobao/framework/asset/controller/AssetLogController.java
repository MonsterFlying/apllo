package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.request.VoAssetLog;
import com.gofobao.framework.security.contants.SecurityContants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by admin on 2017/5/22.
 */
@RestController
@RequestMapping("/assetLog")
@Slf4j
public class AssetLogController {

    @Autowired
    private AssetBiz assetBiz;

    @RequestMapping("/v2/list")
    public ResponseEntity assetLogResList(@ModelAttribute VoAssetLog voAssetLog, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voAssetLog.setUserId(userId);
        return assetBiz.assetLogResList(voAssetLog);
    }


}
