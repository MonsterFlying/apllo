package com.gofobao.framework.repayment.controller;

import com.gofobao.framework.repayment.biz.LoanBiz;
import com.gofobao.framework.repayment.contants.RepaymentContants;
import com.gofobao.framework.repayment.vo.request.VoDetailReq;
import com.gofobao.framework.repayment.vo.request.VoLoanListReq;
import com.gofobao.framework.repayment.vo.response.*;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Created by admin on 2017/6/2.
 */

@Api(description = "我的借款")
@RestController
@RequestMapping("pub/loan")
public class LoanController {
    @Autowired
    private LoanBiz loanBiz;


    @ApiOperation("还款中列表")
    @RequestMapping(value = "/v2/refund/list/{pageIndex}/{pageSize}", method = RequestMethod.GET)
    public ResponseEntity<VoViewRefundWrapRes> refundResList(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                             @PathVariable Integer pageIndex,
                                                             @PathVariable Integer pageSize) {
        return commonQuery(pageIndex, pageSize, userId, RepaymentContants.BUDING);
    }

    @ApiOperation("投标中列表")
    @GetMapping("/v2/budding/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewBuddingResListWrapRes> buddingList(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                                   @PathVariable Integer pageIndex,
                                                                   @PathVariable Integer pageSize) {
        return commonQuery(pageIndex, pageSize, userId, RepaymentContants.REFUND);
    }

    @ApiOperation("已结清列表")
    @GetMapping("/v2/settle/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewSettleWarpListRes> settleList(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId,
                                                              @PathVariable Integer pageIndex,
                                                              @PathVariable Integer pageSize) {
        return commonQuery(pageIndex, pageSize, userId, RepaymentContants.CLOSE);
    }

    @ApiOperation("借款详情")
    @GetMapping("/v2/detail/{borrowId}")
    public ResponseEntity<VoViewRepaymentDetailWrapRes> detail(@PathVariable("borrowId") Long borrowId,
                                                               @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoDetailReq voDetailReq = new VoDetailReq();
        voDetailReq.setUserId(userId);
        voDetailReq.setBorrowId(borrowId);
        return loanBiz.repaymentDetail(voDetailReq);
    }
    @ApiOperation("借款详情列表")
    @GetMapping("/v2/repayment/list/{borrowId}")
    public ResponseEntity<VoViewLoanInfoListWrapRes> repaymentList(@PathVariable("borrowId") Long borrowId,
                                                                   @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoDetailReq voDetailReq = new VoDetailReq();
        voDetailReq.setUserId(userId);
        voDetailReq.setBorrowId(borrowId);
        return loanBiz.loanList(voDetailReq);
    }
    public ResponseEntity commonQuery(Integer pageIndex, Integer pageSize, Long userId, Integer type) {
        VoLoanListReq voLoanListReq = new VoLoanListReq();
        voLoanListReq.setPageIndex(pageIndex);
        voLoanListReq.setPageSize(pageSize);
        voLoanListReq.setUserId(userId);
        switch (type) {
            case 1:
                return loanBiz.refundResList(voLoanListReq);
            case 2:
                return loanBiz.buddingList(voLoanListReq);
            case 3:
                return loanBiz.settleList(voLoanListReq);
        }
        return null;
    }
}