package com.gofobao.framework.asset.controller.web;

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

import javax.servlet.http.HttpServletResponse;

/**
 * Created by admin on 2017/5/22.
 */
@Api(description = "资金流水")
@RestController
@Slf4j
public class WebAssetLogController {

    @Autowired
    private AssetBiz assetBiz;


    @RequestMapping(value = "assetLog/pc/v2/list", method = RequestMethod.POST)
    public ResponseEntity<VoViewAssetLogWarpRes> pcAssetLogResList(@ModelAttribute VoAssetLogReq voAssetLogReq,
                                                                   @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voAssetLogReq.setUserId(userId);
        return assetBiz.newAssetLogResList(voAssetLogReq);
    }


    @ApiOperation("资金流水导出")
    @RequestMapping(value = "assetLog/pc/v2/toExcel", method = RequestMethod.GET)
    public void pcAssetLogToExcel(HttpServletResponse response, @ModelAttribute VoAssetLogReq voAssetLogReq,
                                            @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voAssetLogReq.setUserId(userId);
        assetBiz.pcToExcel(voAssetLogReq,response);
    }
}
