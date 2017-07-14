package com.gofobao.framework.asset.controller.web;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.response.VoAccruedMoneyResp;
import com.gofobao.framework.asset.vo.response.VoAssetIndexResp;
import com.gofobao.framework.asset.vo.response.VoCollectionResp;
import com.gofobao.framework.asset.vo.response.VoUserAssetInfoResp;
import com.gofobao.framework.member.vo.response.pc.ExpenditureDetail;
import com.gofobao.framework.member.vo.response.pc.IncomeEarnedDetail;
import com.gofobao.framework.member.vo.response.pc.VoViewAssetStatisticWarpRes;
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
 * Created by Zeke on 2017/5/19.
 */
@Api(description = "资金模块")
@RestController
public class WebAssetController {

    @Autowired
    private AssetBiz assetBiz;

    @ApiOperation("获取用户资产信息")
    @GetMapping("/asset/pc/v2/info")
    public ResponseEntity<VoUserAssetInfoResp> userAAssessetInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.userAssetInfo(userId);
    }


    @ApiOperation("资产中心数据")
    @GetMapping("/asset/pc/v2/index")
    public ResponseEntity<VoAssetIndexResp> asset(@ApiIgnore  @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.asset(userId) ;
    }

    @ApiOperation("累计收益详情")
    @GetMapping("/asset/pc/v2/accruedMoney")
    public ResponseEntity<VoAccruedMoneyResp> accruedMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.accruedMoney(userId) ;
    }


    @ApiOperation("账户余额")
    @GetMapping("/asset/pc/v2/accountMoney")
    public ResponseEntity<VoViewAssetStatisticWarpRes> accountMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.pcAccountStatstic(userId) ;
    }

    @ApiOperation("待收总额")
    @GetMapping("/asset/pc/v2/collectionMoney")
    public ResponseEntity<VoCollectionResp> collectionMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.collectionMoney(userId) ;
    }

    @ApiOperation("账户总额统计")
    @GetMapping("/asset/pc/v2/accountTotal")
    public ResponseEntity<VoViewAssetStatisticWarpRes> accountTotal(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.pcAccountStatstic(userId);
    }
    @ApiOperation("总收益统计詳情")
    @GetMapping("/asset/pc/v2/incomeEarnedTotal")
    public ResponseEntity<IncomeEarnedDetail> incomeEarnedTotal(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.pcIncomeEarned(userId);
    }

    @ApiOperation("总支出明细统计")
    @GetMapping("/asset/pc/v2/expenditureDetail")
    public ResponseEntity<ExpenditureDetail> expenditureDetail(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.pcExpenditureDetail(userId);
    }




}
