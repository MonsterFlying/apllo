package com.gofobao.framework.tender.controller;

import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.tender.biz.MyInvestBiz;
import com.gofobao.framework.tender.service.InvestService;
import com.gofobao.framework.tender.vo.request.VoDetailReq;
import com.gofobao.framework.tender.vo.request.VoInvestListReq;
import com.gofobao.framework.tender.vo.response.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by admin on 2017/6/1.
 */
@RequestMapping("/invest")
@ApiModel("我的投资")
@RestController
public class MyInvestController {

    @Autowired
    private InvestService investService;

    @Autowired
    private MyInvestBiz investBiz;

    /**
     * 回款中列表
     *
     * @return
     */
    @ApiOperation("回款中列表")
    @GetMapping("/v2/backMoney/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewBackMoneyListWarpRes> backMoneyList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                    @PathVariable Integer pageIndex,
                                                                    @PathVariable Integer pageSize) {
        VoInvestListReq voInvestListReq = new VoInvestListReq();
        voInvestListReq.setUserId(userId);
        voInvestListReq.setPageIndex(pageIndex);
        voInvestListReq.setPageSize(pageSize);
        return investBiz.backMoneyList(voInvestListReq);
    }


    @ApiOperation("投标中列表")
    @GetMapping("/v2/bidding/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewBiddingListWrapRes> biddingList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                @PathVariable Integer pageIndex,
                                                                @PathVariable Integer pageSize) {
        VoInvestListReq voInvestListReq = new VoInvestListReq();
        voInvestListReq.setUserId(userId);
        voInvestListReq.setPageIndex(pageIndex);
        voInvestListReq.setPageSize(pageSize);
        return investBiz.biddingList(voInvestListReq);
    }


    @ApiOperation("已结清列表")
    @GetMapping("/v2/settle/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewSettleWarpRes> settleList(@RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                          @PathVariable Integer pageIndex,
                                                          @PathVariable Integer pageSize) {
        VoInvestListReq voInvestListReq = new VoInvestListReq();
        voInvestListReq.setUserId(userId);
        voInvestListReq.setPageIndex(pageIndex);
        voInvestListReq.setPageSize(pageSize);
        return investBiz.settleList(voInvestListReq);
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


}
