package com.gofobao.framework.finance.controller;

import com.gofobao.framework.finance.biz.MyFinanceInvestBiz;
import com.gofobao.framework.security.contants.SecurityContants;
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
@RequestMapping("/finance/invest/")
@Api(description="pc：我的投资")
@RestController
public class FinanceInvestController {

    @Autowired
    private MyFinanceInvestBiz financeInvestBiz;

    /**
     * pc：回款中列表
     *
     * @returnz
     */
    @ApiOperation("pc:回款中列表")
    @GetMapping("v2/backMoney/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewBackMoneyListWarpRes> pcBackMoneyList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                    @PathVariable Integer pageIndex,
                                                                    @PathVariable Integer pageSize) {
       return pcBackMoneyCommonResult( pageIndex, pageSize, userId);
    }



    @ApiOperation("pc:投标中列表")
    @GetMapping("v2/bidding/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewBiddingListWrapRes> pcBiddingList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                @PathVariable Integer pageIndex,
                                                                @PathVariable Integer pageSize) {
       return pcBuildCommonResult( pageIndex, pageSize, userId);
    }

    @ApiOperation("pc:已结清列表")
    @GetMapping("v2/settle/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewSettleWarpRes> pcSettleList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                          @PathVariable Integer pageIndex,
                                                          @PathVariable Integer pageSize) {
        return pcSettleCommonResult( pageIndex, pageSize, userId);
    }


    @ApiOperation("pc:投资详情")
    @GetMapping("v2/tender/detail/{tenderId}")
    public ResponseEntity<VoViewTenderDetailWarpRes> pcTenderDetail(@PathVariable Long tenderId, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoDetailReq voDetailReq = new VoDetailReq();
        voDetailReq.setUserId(userId);
        voDetailReq.setTenderId(tenderId);
        return financeInvestBiz.tenderDetail(voDetailReq);
    }

    @ApiOperation("pc:投资详情列表")
    @GetMapping("/v2/tender/collection/{tenderId}")
    public ResponseEntity<VoViewReturnMoneyWarpRes> pcInfoList(@PathVariable Long tenderId,
                                                             @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoDetailReq voDetailReq = new VoDetailReq();
        voDetailReq.setUserId(userId);
        voDetailReq.setTenderId(tenderId);
        return financeInvestBiz.infoList(voDetailReq);
    }

    private ResponseEntity<VoViewSettleWarpRes> pcSettleCommonResult(Integer pageIndex, Integer pageSize, Long userId){
        VoInvestListReq voInvestListReq = new VoInvestListReq();
        voInvestListReq.setUserId(userId);
        voInvestListReq.setPageIndex(pageIndex);
        voInvestListReq.setPageSize(pageSize);
        return financeInvestBiz.settleList(voInvestListReq);
    }
    private ResponseEntity<VoViewBackMoneyListWarpRes> pcBackMoneyCommonResult(Integer pageIndex, Integer pageSize, Long userId){
        VoInvestListReq voInvestListReq = new VoInvestListReq();
        voInvestListReq.setUserId(userId);
        voInvestListReq.setPageIndex(pageIndex);
        voInvestListReq.setPageSize(pageSize);
        return financeInvestBiz.backMoneyList(voInvestListReq);
    }
    private ResponseEntity<VoViewBiddingListWrapRes> pcBuildCommonResult(Integer pageIndex, Integer pageSize, Long userId){
        VoInvestListReq voInvestListReq = new VoInvestListReq();
        voInvestListReq.setUserId(userId);
        voInvestListReq.setPageIndex(pageIndex);
        voInvestListReq.setPageSize(pageSize);
        return financeInvestBiz.biddingList(voInvestListReq);
    }
}
