package com.gofobao.framework.tender.controller;

import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.biz.MyInvestBiz;
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
@RequestMapping("/invest")
@Api(description="我的投资")
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
        return backMoneyCommonResult( pageIndex, pageSize, userId);
    }

    /**
     * pc：回款中列表
     *
     * @returnz
     */
    @ApiOperation("pc:回款中列表")
    @GetMapping("pc/v2/backMoney/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewBackMoneyListWarpRes> pcBackMoneyList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                    @PathVariable Integer pageIndex,
                                                                    @PathVariable Integer pageSize) {
       return backMoneyCommonResult( pageIndex, pageSize, userId);
    }


    @ApiOperation("投标中列表")
    @GetMapping("/v2/bidding/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewBiddingListWrapRes> biddingList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                @PathVariable Integer pageIndex,
                                                                @PathVariable Integer pageSize) {
        return buildCommonResult( pageIndex, pageSize, userId);
    }

    @ApiOperation("pc:投标中列表")
    @GetMapping("pc/v2/bidding/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewBiddingListWrapRes> pcBiddingList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                @PathVariable Integer pageIndex,
                                                                @PathVariable Integer pageSize) {
       return buildCommonResult( pageIndex, pageSize, userId);
    }



    @ApiOperation("已结清列表")
    @GetMapping("/v2/settle/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewSettleWarpRes> settleList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                          @PathVariable Integer pageIndex,
                                                          @PathVariable Integer pageSize) {

        return settleCommonResult( pageIndex, pageSize, userId);
    }

    @ApiOperation("pc:已结清列表")
    @GetMapping("pc/v2/settle/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewSettleWarpRes> pcSettleList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                          @PathVariable Integer pageIndex,
                                                          @PathVariable Integer pageSize) {
        return settleCommonResult( pageIndex, pageSize, userId);
    }


    @ApiOperation("投资详情")
    @GetMapping("/v2/tender/detail/{tenderId}")
    public ResponseEntity<VoViewTenderDetailWarpRes> tenderDetail(@PathVariable Long tenderId, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoDetailReq voDetailReq = new VoDetailReq();
        voDetailReq.setUserId(userId);
        voDetailReq.setTenderId(tenderId);
        return investBiz.tenderDetail(voDetailReq);
    }

    @ApiOperation("投资详情")
    @GetMapping("/v2/tender/collection/{tenderId}")
    public ResponseEntity<VoViewReturnMoneyWarpRes> infoList(@PathVariable Long tenderId,
                                                             @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoDetailReq voDetailReq = new VoDetailReq();
        voDetailReq.setUserId(userId);
        voDetailReq.setTenderId(tenderId);
        return investBiz.infoList(voDetailReq);
    }

    private ResponseEntity<VoViewSettleWarpRes>settleCommonResult(Integer pageIndex,Integer pageSize,Long userId){
        VoInvestListReq voInvestListReq = new VoInvestListReq();
        voInvestListReq.setUserId(userId);
        voInvestListReq.setPageIndex(pageIndex);
        voInvestListReq.setPageSize(pageSize);
        return investBiz.settleList(voInvestListReq);
    }
    private ResponseEntity<VoViewBackMoneyListWarpRes>backMoneyCommonResult(Integer pageIndex,Integer pageSize,Long userId){
        VoInvestListReq voInvestListReq = new VoInvestListReq();
        voInvestListReq.setUserId(userId);
        voInvestListReq.setPageIndex(pageIndex);
        voInvestListReq.setPageSize(pageSize);
        return investBiz.backMoneyList(voInvestListReq);
    }
    private ResponseEntity<VoViewBiddingListWrapRes>buildCommonResult(Integer pageIndex,Integer pageSize,Long userId){
        VoInvestListReq voInvestListReq = new VoInvestListReq();
        voInvestListReq.setUserId(userId);
        voInvestListReq.setPageIndex(pageIndex);
        voInvestListReq.setPageSize(pageSize);
        return investBiz.biddingList(voInvestListReq);
    }
}
