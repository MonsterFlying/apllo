package com.gofobao.framework.tender.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.biz.MyInvestBiz;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.vo.request.VoDetailReq;
import com.gofobao.framework.tender.vo.request.VoInvestListReq;
import com.gofobao.framework.tender.vo.response.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by admin on 2017/6/1.
 */
@RequestMapping("pub/invest")
@Api(description = "我的投资")
@RestController
public class MyInvestController {

    @Autowired
    private MyInvestBiz investBiz;

    /**
     * 回款中列表
     *
     * @returnz
     */
    @ApiOperation("回款中列表")
    @GetMapping("/v2/backMoney/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewBackMoneyListWarpRes> backMoneyList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                    @PathVariable Integer pageIndex,
                                                                    @PathVariable Integer pageSize) {
        return commonResult(pageIndex, pageSize, userId, TenderConstans.BACK_MONEY);
    }


    @ApiOperation("投标中列表")
    @GetMapping("/v2/bidding/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewBiddingListWrapRes> biddingList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                @PathVariable Integer pageIndex,
                                                                @PathVariable Integer pageSize) {
        return commonResult(pageIndex, pageSize, userId, TenderConstans.BIDDING);
    }

    @ApiOperation("已结清列表")
    @GetMapping("/v2/settle/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewSettleWarpRes> settleList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                          @PathVariable Integer pageIndex,
                                                          @PathVariable Integer pageSize) {
        return commonResult(pageIndex, pageSize, userId, TenderConstans.SETTLE);
    }

    @ApiOperation("投资详情")
    @GetMapping("/v2/tender/detail/{tenderId}")
    public ResponseEntity<VoViewTenderDetailWarpRes> tenderDetail(@PathVariable Long tenderId, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoDetailReq voDetailReq = new VoDetailReq();
        voDetailReq.setUserId(userId);
        voDetailReq.setTenderId(tenderId);
        return investBiz.tenderDetail(voDetailReq);
    }

    @ApiOperation("回款详情")
    @GetMapping("/v2/tender/collection/{tenderId}")
    public ResponseEntity<VoViewReturnMoneyWarpRes> infoList(@PathVariable Long tenderId,
                                                             @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoDetailReq voDetailReq = new VoDetailReq();
        voDetailReq.setUserId(userId);
        voDetailReq.setTenderId(tenderId);
        return investBiz.infoList(voDetailReq);
    }

    private ResponseEntity commonResult(Integer pageIndex, Integer pageSize, Long userId, Integer type) {

        VoInvestListReq voInvestListReq = new VoInvestListReq();
        voInvestListReq.setUserId(userId);
        voInvestListReq.setPageIndex(pageIndex);
        voInvestListReq.setPageSize(pageSize);
        switch (type) {
            case 1:
                return investBiz.biddingList(voInvestListReq);  //投标中
            case 2:
                return investBiz.backMoneyList(voInvestListReq);  //回款中
            case 3:
                return investBiz.settleList(voInvestListReq);  //已结清
        }
        return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewReturnMoneyWarpRes.class));
    }


}
