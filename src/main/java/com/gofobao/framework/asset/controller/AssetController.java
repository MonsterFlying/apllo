package com.gofobao.framework.asset.controller;

import com.gofobao.framework.as.biz.AssetStatementBiz;
import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.request.VoSynAssetsRep;
import com.gofobao.framework.asset.vo.response.*;
import com.gofobao.framework.borrow.vo.request.VoDoAgainVerifyReq;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.project.SecurityHelper;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.vo.request.VoCommonReq;
import com.gofobao.framework.tender.vo.request.VoLocalAssetChangeReq;
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

    @Autowired
    AssetStatementBiz assetStatementBiz;

    @PostMapping("pub/asset/check-up-all-account")
    public ResponseEntity<VoBaseResp> checkUpAllAccount(@ModelAttribute VoCommonReq voCommonReq) {
        String paramStr = voCommonReq.getParamStr();
        if (!SecurityHelper.checkSign(voCommonReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "本地记录修改异常!"));
        }

        assetStatementBiz.checkUpAccountForAll();
        return ResponseEntity.ok(VoBaseResp.ok("成功"));
    }

    @PostMapping("pub/asset/check-up-active-account")
    public ResponseEntity<VoBaseResp> checkUpActiveAccount(@ModelAttribute VoCommonReq voCommonReq) {
        String paramStr = voCommonReq.getParamStr();
        if (!SecurityHelper.checkSign(voCommonReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "本地记录修改异常!"));
        }

        assetStatementBiz.checkUpAccountForActiveState();
        return ResponseEntity.ok(VoBaseResp.ok("成功"));
    }




    @PostMapping("pub/asset/check-up-partial-account")
    public ResponseEntity<VoBaseResp> checkUpPartialAccount(@ModelAttribute VoCommonReq voCommonReq) {
        String paramStr = voCommonReq.getParamStr();
        if (!SecurityHelper.checkSign(voCommonReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "本地记录修改异常!"));
        }

        assetStatementBiz.checkUpAccountForChange();
        return ResponseEntity.ok(VoBaseResp.ok("成功"));
    }


    @PostMapping("pub/asset/changeRecord")
    public ResponseEntity<VoBaseResp> insertLocalRecord(@ModelAttribute VoLocalAssetChangeReq voLocalAssetChangeReq) throws Exception {
        String paramStr = voLocalAssetChangeReq.getParamStr();
        if (!SecurityHelper.checkSign(voLocalAssetChangeReq.getSign(), paramStr)) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "本地记录修改异常!"));
        }

        return assetBiz.changeLocalAsset(voLocalAssetChangeReq);
    }

    @ApiOperation("获取用户资产信息")
    @GetMapping("/asset/v2/info")
    public ResponseEntity<VoUserAssetInfoResp> userAAssessetInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.userAssetInfo(userId);
    }


    @ApiOperation("投标中心资金同步问题")
    @PostMapping("/asset/v2/synOffLineRecharge")
    public ResponseEntity<VoUserAssetInfoResp> synOffLineRecharge(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        return assetBiz.synOffLineRecharge(userId);
    }


    @ApiOperation("资产中心资金同步问题")
    @PostMapping("/home/v2/synHome")
    public ResponseEntity<VoAssetIndexResp> synHome(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        return assetBiz.synHome(userId);
    }

    @ApiOperation("资金同步问题")
    @PostMapping("/pub/asset/v2/adminSynOffLineRecharge")
    public ResponseEntity<VoUserAssetInfoResp> adminSynOffLineRecharge(@ModelAttribute VoSynAssetsRep voSynAssetsRep) throws Exception {
        return assetBiz.adminSynOffLineRecharge(voSynAssetsRep);
    }


    @ApiOperation("资产中心数据")
    @GetMapping("/asset/v2/index")
    public ResponseEntity<VoAssetIndexResp> asset(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.asset(userId);
    }

    @ApiOperation("累计收益详情")
    @GetMapping("/asset/v2/accruedMoney")
    public ResponseEntity<VoAccruedMoneyResp> accruedMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.accruedMoney(userId);
    }

    @ApiOperation("累计支出详情")
    @GetMapping("/asset/v2/expandMoney")
    public ResponseEntity<VoExpenditureResp> expandMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.expandMoney(userId);
    }

    @ApiOperation("净资产详情")
    @GetMapping("/asset/v2/netAssetDetail")
    public ResponseEntity<VoAssetDetailResp> netAssetDetail(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId){
        return assetBiz.netAssetDetail(userId);
    }

    @ApiOperation("账户余额")
    @GetMapping("/asset/v2/accountMoney")
    public ResponseEntity<VoAvailableAssetInfoResp> accountMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.accountMoney(userId);
    }

    @ApiOperation("待收总额")
    @GetMapping("/asset/v2/collectionMoney")
    public ResponseEntity<VoCollectionResp> collectionMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.collectionMoney(userId);
    }


    /**
     * 查询用户金额
     *
     * @param voDoAgainVerifyReq
     * @return
     */
    @PostMapping("/pub/asset/money")
    @ApiOperation("实时查询用户金额")
    public ResponseEntity<VoQueryInfoResp> queryUserMoneyForJixin(VoDoAgainVerifyReq voDoAgainVerifyReq) {
        return assetBiz.queryUserMoneyForJixin(voDoAgainVerifyReq);
    }

}
