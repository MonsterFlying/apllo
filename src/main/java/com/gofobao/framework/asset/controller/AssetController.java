package com.gofobao.framework.asset.controller;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.request.VoSynAssetsRep;
import com.gofobao.framework.asset.vo.response.*;
import com.gofobao.framework.member.vo.response.VoBasicUserInfoResp;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

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
    public ResponseEntity<VoUserAssetInfoResp> userAAssessetInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.userAssetInfo(userId);
    }


    @ApiOperation("投标中心资金同步问题")
    @PostMapping("/asset/v2/synOffLineRecharge")
    public ResponseEntity<VoUserAssetInfoResp> synOffLineRecharge(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        return assetBiz.synOffLineRecharge(userId) ;
    }


    @ApiOperation("资产中心资金同步问题")
    @PostMapping("/home/v2/synHome")
    public ResponseEntity<VoAssetIndexResp> synHome(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        return assetBiz.synHome(userId) ;
    }

    @ApiOperation("资金同步问题")
    @PostMapping("/pub/asset/v2/adminSynOffLineRecharge")
    public ResponseEntity<VoUserAssetInfoResp> adminSynOffLineRecharge(@ModelAttribute VoSynAssetsRep voSynAssetsRep) throws Exception {
        return assetBiz.adminSynOffLineRecharge(voSynAssetsRep) ;
    }


    @ApiOperation("资产中心数据")
    @GetMapping("/asset/v2/index")
    public ResponseEntity<VoAssetIndexResp> asset(@ApiIgnore  @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.asset(userId) ;
    }

    @ApiOperation("累计收益详情")
    @GetMapping("/asset/v2/accruedMoney")
    public ResponseEntity<VoAccruedMoneyResp> accruedMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.accruedMoney(userId) ;
    }


    @ApiOperation("账户余额")
    @GetMapping("/asset/v2/accountMoney")
    public ResponseEntity<VoAvailableAssetInfoResp> accountMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.accountMoney(userId) ;
    }

    @ApiOperation("待收总额")
    @GetMapping("/asset/v2/collectionMoney")
    public ResponseEntity<VoCollectionResp> collectionMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.collectionMoney(userId) ;
    }
}
