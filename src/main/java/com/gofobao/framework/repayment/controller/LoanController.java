package com.gofobao.framework.repayment.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.biz.LoanBiz;
import com.gofobao.framework.repayment.service.LoanService;
import com.gofobao.framework.repayment.vo.request.VoDetailReq;
import com.gofobao.framework.repayment.vo.request.VoLoanListReq;
import com.gofobao.framework.repayment.vo.response.*;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * Created by admin on 2017/6/2.
 */

@ApiModel("我的借款")
@RestController
@RequestMapping("/loan")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanBiz loanBiz;

    @ApiOperation("还款中列表")
    @RequestMapping(value = "/v2/refund/list",method = RequestMethod.GET,consumes ="application/json; charset=utf-8")
    public ResponseEntity<List<VoViewRefundRes>>  refundResList(/*@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId*/ ) {
        VoLoanListReq voLoanListReq = new VoLoanListReq();
        Long userId = 901L;
        voLoanListReq.setUserId(userId);
        return  loanBiz.refundResList(voLoanListReq);
    }

    @ApiOperation("投标中列表")
    @GetMapping("/v2/budding/list")
    public ResponseEntity buddingList(/*@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId*/) {
        VoLoanListReq voLoanListReq = new VoLoanListReq();
        Long userId = 901L;
        voLoanListReq.setUserId(userId);
        try {
            List<VoViewBudingRes> voViewRefundResList = loanService.buddingList(voLoanListReq);
            return ResponseEntity.ok(voViewRefundResList);
        } catch (Exception e) {
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "还款中列表失败!"));
        }
    }

    @ApiOperation("已结清列表")
    @GetMapping("/v2/settle/list")
    public ResponseEntity settleList(/*@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId*/) {
        VoLoanListReq voLoanListReq = new VoLoanListReq();
        Long userId = 901L;
        voLoanListReq.setUserId(userId);
        try {
            List<VoViewSettleRes> voViewSettleRes = loanService.settleList(voLoanListReq);
            return ResponseEntity.ok(voViewSettleRes);
        } catch (Exception e) {
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "还款中列表失败!"));
        }
    }


    @ApiOperation("已结清列表")
    @GetMapping("/v2/detail/{borrowId}")
    public ResponseEntity detail(@PathVariable("borrowId") Long borrowId/*@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId*/) {
        Long userId = 901L;
        VoDetailReq voDetailReq=new VoDetailReq();
        voDetailReq.setUserId(userId);
        voDetailReq.setBorrowId(borrowId);
        try {
            VoViewRepaymentDetail voViewSettleRes =loanService.repaymentDetail(voDetailReq);
            return ResponseEntity.ok(voViewSettleRes);
        } catch (Exception e) {
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "还款中列表失败!"));
        }
    }


    @ApiOperation("借款详情列表")
    @GetMapping("/v2/repayment/list/{borrowId}")
    public ResponseEntity repaymentList(@PathVariable("borrowId") Long borrowId/*@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId*/) {
        Long userId = 901L;
        VoDetailReq voDetailReq=new VoDetailReq();
        voDetailReq.setUserId(userId);
        voDetailReq.setBorrowId(borrowId);
        try {
            VoViewLoanList voViewLoanList =loanService.loanList(voDetailReq);
            return ResponseEntity.ok(voViewLoanList);
        } catch (Exception e) {
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "还款中列表失败!"));
        }
    }


}