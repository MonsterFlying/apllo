package com.gofobao.framework.finance.controller;

import com.gofobao.framework.finance.biz.MyFinanceInvestBiz;
import com.gofobao.framework.finance.vo.response.VoViewFinanceTenderDetailWarpRes;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.system.vo.request.VoFinanceDetailReq;
import com.gofobao.framework.system.vo.response.VoViewFinanceReturnMoneyWarpRes;
import com.gofobao.framework.tender.vo.request.VoFinanceInvestListReq;
import com.gofobao.framework.tender.vo.response.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by admin on 2017/6/1.
 */
@RequestMapping("/financeserver/invest")
@Api(description="pc：我的投资")
@RestController
public class FinanceServerInvestController {

    @Autowired
    private MyFinanceInvestBiz financeInvestBiz;

    /**
     * pc：回款中列表
     *
     * @returnz
     */
    @ApiOperation("pc:回款中列表")
    @GetMapping("v2/backMoney/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewFinanceBackMoneyListWarpRes> pcBackMoneyList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                             @PathVariable Integer pageIndex,
                                                                             @PathVariable Integer pageSize) {
        VoFinanceInvestListReq voFinanceInvestListReq = new VoFinanceInvestListReq();
        voFinanceInvestListReq.setUserId(userId);
        voFinanceInvestListReq.setPageIndex(pageIndex);
        voFinanceInvestListReq.setPageSize(pageSize);
        return financeInvestBiz.backMoneyList(voFinanceInvestListReq);
    }



    @ApiOperation("pc:投标中列表")
    @GetMapping("v2/bidding/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewFinanceBiddingListWrapRes> pcBiddingList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                         @PathVariable Integer pageIndex,
                                                                         @PathVariable Integer pageSize) {
        VoFinanceInvestListReq voFinanceInvestListReq = new VoFinanceInvestListReq();
        voFinanceInvestListReq.setUserId(userId);
        voFinanceInvestListReq.setPageIndex(pageIndex);
        voFinanceInvestListReq.setPageSize(pageSize);
        return financeInvestBiz.biddingList(voFinanceInvestListReq);
    }

    @ApiOperation("pc:已结清列表")
    @GetMapping("v2/settle/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewFinanceSettleWarpRes> pcSettleList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                          @PathVariable Integer pageIndex,
                                                          @PathVariable Integer pageSize) {
        VoFinanceInvestListReq voFinanceInvestListReq = new VoFinanceInvestListReq();
        voFinanceInvestListReq.setUserId(userId);
        voFinanceInvestListReq.setPageIndex(pageIndex);
        voFinanceInvestListReq.setPageSize(pageSize);
        return financeInvestBiz.settleList(voFinanceInvestListReq);
    }


    @ApiOperation("pc:投资详情")
    @GetMapping("v2/tender/detail/{buyerId}")
    public ResponseEntity<VoViewFinanceTenderDetailWarpRes> pcTenderDetail(@PathVariable Long buyerId, @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoFinanceDetailReq voDetailReq = new VoFinanceDetailReq();
        voDetailReq.setUserId(userId);
        voDetailReq.setBuyerId(buyerId);
        return financeInvestBiz.tenderDetail(voDetailReq);
    }

    @ApiOperation("pc:投资详情列表")
    @GetMapping("/v2/tender/collection/{buyerId}")
    public ResponseEntity<VoViewFinanceReturnMoneyWarpRes> pcInfoList(@PathVariable Long buyerId,
                                                                      @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoFinanceDetailReq voDetailReq = new VoFinanceDetailReq();
        voDetailReq.setUserId(userId);
        voDetailReq.setBuyerId(buyerId);
        return financeInvestBiz.infoList(voDetailReq);
    }
}
