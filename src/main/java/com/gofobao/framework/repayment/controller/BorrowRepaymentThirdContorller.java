package com.gofobao.framework.repayment.controller;

import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by admin on 2017/6/1.
 */
@RestController
@Api(description = "还款计划")
@RequestMapping("/pub/repayment")
public class BorrowRepaymentThirdContorller {

    @Autowired
    private BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;

    @RequestMapping("/v2/third/batch/lendrepay/check")
    @ApiOperation("批次放款参数检查通知")
    public ResponseEntity<String> thirdBatchLendRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        return borrowRepaymentThirdBiz.thirdBatchLendRepayCheckCall(request, response);
    }

    @RequestMapping("/v2/third/batch/lendrepay/run")
    @ApiOperation("批次放款运行结果通知")
    public ResponseEntity<String> thirdBatchLendRepayRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return borrowRepaymentThirdBiz.thirdBatchLendRepayRunCall(request, response);
    }

    @RequestMapping("/v2/third/batch/repayDeal/check")
    @ApiOperation("批次还款参数检查通知")
    public ResponseEntity<String> thirdBatchRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        return borrowRepaymentThirdBiz.thirdBatchRepayCheckCall(request, response);
    }

    @RequestMapping("/v2/third/batch/repayDeal/run")
    @ApiOperation("批次还款参数检查通知")
    public ResponseEntity<String> thirdBatchRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
        return borrowRepaymentThirdBiz.thirdBatchRepayRunCall(request, response);
    }

    @RequestMapping("/v2/third/batch/bailrepay/check")
    @ApiOperation("批次担保账户代偿参数检查回调")
    public ResponseEntity<String> thirdBatchBailRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        return borrowRepaymentThirdBiz.thirdBatchBailRepayCheckCall(request, response);
    }

    @RequestMapping("/v2/third/batch/bailrepay/run")
    @ApiOperation("批次担保账户代偿业务处理回调")
    public ResponseEntity<String> thirdBatchBailRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
        return borrowRepaymentThirdBiz.thirdBatchBailRepayRunCall(request, response);
    }

    @RequestMapping("/v2/third/batch/repaybail/check")
    @ApiOperation("批次融资人还担保账户垫款参数检查回调")
    public ResponseEntity<String> thirdBatchRepayBailCheckCall(HttpServletRequest request, HttpServletResponse response) {
        return borrowRepaymentThirdBiz.thirdBatchRepayBailCheckCall(request, response);
    }

    @RequestMapping("/v2/third/batch/repaybail/run")
    @ApiOperation("批次融资人还担保账户垫款业务处理回调")
    public ResponseEntity<String> thirdBatchRepayBailRunCall(HttpServletRequest request, HttpServletResponse response) {
        return borrowRepaymentThirdBiz.thirdBatchRepayBailRunCall(request, response);
    }
}
