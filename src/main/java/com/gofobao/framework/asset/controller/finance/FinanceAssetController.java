package com.gofobao.framework.asset.controller.finance;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.response.VoUserAssetInfoResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by admin on 2017/8/15.
 */

@Api(description = "资金模块")
@RestController
public class FinanceAssetController {

    @Autowired
    private AssetBiz assetBiz;

    @ApiOperation("获取用户资产信息")
    @GetMapping("/asset/finance/v2/info")
    public ResponseEntity<VoUserAssetInfoResp> userAssetInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.userAssetInfo(userId);
    }

}
