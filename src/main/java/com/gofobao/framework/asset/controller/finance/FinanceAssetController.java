package com.gofobao.framework.asset.controller.finance;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.request.VoSynAssetsRep;
import com.gofobao.framework.asset.vo.response.VoAccruedMoneyResp;
import com.gofobao.framework.asset.vo.response.VoAssetIndexResp;
import com.gofobao.framework.asset.vo.response.VoDueInRes;
import com.gofobao.framework.asset.vo.response.VoUserAssetInfoResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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


    @ApiOperation("投标中心资金同步问题")
    @PostMapping("/asset/finance/v2/synOffLineRecharge")
    public ResponseEntity<VoUserAssetInfoResp> synOffLineRecharge(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        return assetBiz.synOffLineRecharge(userId);
    }


    @ApiOperation("资产中心资金同步问题")
    @PostMapping("/home/finance/v2/synHome")
    public ResponseEntity<VoAssetIndexResp> synHome(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        return assetBiz.synHome(userId);
    }

    @ApiOperation("资产中心数据")
    @GetMapping("/asset/finance/v2/index")
    public ResponseEntity<VoAssetIndexResp> asset(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.asset(userId);
    }

    @ApiOperation("累计收益详情")
    @GetMapping("/asset/finance/v2/accruedMoney")
    public ResponseEntity<VoAccruedMoneyResp> accruedMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.accruedMoney(userId);
    }

    @ApiOperation("待收")
    @GetMapping("asset/finance/v2/dueIn")
    public ResponseEntity<VoDueInRes> dueIn(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.dueInInfo(userId);

    }


}
