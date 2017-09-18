package com.gofobao.framework.asset.controller.web;

import com.gofobao.framework.asset.biz.AssetBiz;
import com.gofobao.framework.asset.vo.request.VoSendRedPacket;
import com.gofobao.framework.asset.vo.request.VoUnsendRedPacket;
import com.gofobao.framework.asset.vo.response.VoAccruedMoneyResp;
import com.gofobao.framework.asset.vo.response.VoAssetIndexResp;
import com.gofobao.framework.asset.vo.response.VoCollectionResp;
import com.gofobao.framework.asset.vo.response.VoUserAssetInfoResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.response.pc.ExpenditureDetail;
import com.gofobao.framework.member.vo.response.pc.IncomeEarnedDetail;
import com.gofobao.framework.member.vo.response.pc.VoViewAssetStatisticWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/5/19.
 */
@Api(description = "资金模块")
@RestController
@Slf4j
public class WebAssetController {

    @Autowired
    private AssetBiz assetBiz;


    /**
     * 发送即信红包
     *
     * @param voSendRedPacket
     * @return
     */
    @ApiOperation("发送即信红包")
    @PostMapping("/pub/asset/pc/v2/redpacket/send")
    public ResponseEntity<VoBaseResp> sendRedPacket(VoSendRedPacket voSendRedPacket) {
        return assetBiz.sendRedPacket(voSendRedPacket);
    }

    /**
     * 撤回即信红包
     *
     * @param voUnsendRedPacket
     * @return
     */
    @ApiOperation("撤回即信红包")
    @PostMapping("/pub/asset/pc/v2/redpacket/unsend")
    public ResponseEntity<VoBaseResp> unsendRedPacket(@Valid @ModelAttribute VoUnsendRedPacket voUnsendRedPacket) {
        return assetBiz.unsendRedPacket(voUnsendRedPacket);
    }


    /**
     * 撤回即信红包
     *
     * @param voUnsendRedPacket
     * @return
     */
    @ApiOperation("撤回即信红包")
    @PostMapping("/pub/asset/pc/v2/redpacket/cancel")
    public ResponseEntity<VoBaseResp> cancelRedPacket(@Valid @ModelAttribute VoUnsendRedPacket voUnsendRedPacket) {
        try {
            return assetBiz.cancelRedPacket(voUnsendRedPacket);
        } catch (Exception e) {
            log.error("红包撤销处理失败");
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, e.getMessage()));
        }
    }


    /**
     * 撤回即信红包
     *
     * @param voUnsendRedPacket
     * @return
     */
    @ApiOperation("撤回即信红包")
    @PostMapping("/pub/asset/pc/v2/redpacket/cancelAndNoLog")
    public ResponseEntity<VoBaseResp> cancelRedPacketNoChangeLog(@Valid @ModelAttribute VoUnsendRedPacket voUnsendRedPacket) {
        try {
            return assetBiz.cancelRedPacketNoChangeLog(voUnsendRedPacket);
        } catch (Exception e) {
            log.error("红包撤销处理失败");
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, e.getMessage()));
        }
    }

    @ApiOperation("获取用户资产信息")
    @GetMapping("/asset/pc/v2/info")
    public ResponseEntity<VoUserAssetInfoResp> userAAssessetInfo(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.userAssetInfo(userId);
    }


    @ApiOperation("资产中心数据")
    @GetMapping("/asset/pc/v2/index")
    public ResponseEntity<VoAssetIndexResp> asset(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.asset(userId);
    }

    @ApiOperation("累计收益详情")
    @GetMapping("/asset/pc/v2/accruedMoney")
    public ResponseEntity<VoAccruedMoneyResp> accruedMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.accruedMoney(userId);
    }


    @ApiOperation("账户余额")
    @GetMapping("/asset/pc/v2/accountMoney")
    public ResponseEntity<VoViewAssetStatisticWarpRes> accountMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.pcAccountStatstic(userId);
    }

    @ApiOperation("待收总额")
    @GetMapping("/asset/pc/v2/collectionMoney")
    public ResponseEntity<VoCollectionResp> collectionMoney(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        return assetBiz.collectionMoney(userId);
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

    @ApiOperation("资产中心资金同步问题")
    @PostMapping("/home/pc/v2/synHome")
    public ResponseEntity<VoAssetIndexResp> synHome(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) throws Exception {
        return assetBiz.synHome(userId);
    }

}
