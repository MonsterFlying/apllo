package com.gofobao.framework.asset.controller.finance;

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
public class FinanceAssetLogController {

    @Autowired
    private AssetBiz assetBiz;

    @ApiOperation("新版资金流水")
    @RequestMapping(value = "pub/finance/assetLog/v2/list",method = RequestMethod.POST)
    public ResponseEntity<VoViewAssetLogWarpRes> newAssetLogResList(@ModelAttribute VoAssetLogReq voAssetLogReq,
                                                                 @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        voAssetLogReq.setUserId(userId);
        return assetBiz.newAssetLogResList(voAssetLogReq);
    }

}
