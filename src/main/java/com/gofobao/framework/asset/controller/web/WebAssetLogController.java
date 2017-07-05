package com.gofobao.framework.asset.controller.web;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.request.VoAssetLogReq;
import com.gofobao.framework.asset.vo.response.pc.VoViewAssetLogsWarpRes;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by admin on 2017/5/22.
 */
@Api(description = "资金流水")
@RestController
@Slf4j
public class WebAssetLogController {

    @Autowired
    private AssetBiz assetBiz;

    @RequestMapping(value = "pub/assetLog/pc/v2/list", method = RequestMethod.POST)
    public ResponseEntity<VoViewAssetLogsWarpRes> pcAssetLogResList(@ModelAttribute VoAssetLogReq voAssetLogReq/*,
                                                                    @RequestAttribute(SecurityContants.USERID_KEY) Long userId*/) {
        voAssetLogReq.setUserId(901L);
        return assetBiz.pcAssetLogs(voAssetLogReq);
    }


}
