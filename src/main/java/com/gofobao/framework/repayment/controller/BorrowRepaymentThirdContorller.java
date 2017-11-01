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
    public ResponseEntity<String> thirdBatchLendRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
        try {
            return borrowRepaymentThirdBiz.thirdBatchLendRepayRunCall(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("error");
        }
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

    @RequestMapping("/v2/third/batch/advance/check")
    @ApiOperation("批次名义借款人垫付参数检查回调")
    public ResponseEntity<String> thirdBatchAdvanceCheckCall(HttpServletRequest request, HttpServletResponse response) {
        return borrowRepaymentThirdBiz.thirdBatchAdvanceCheckCall(request, response);
    }

    @RequestMapping("/v2/third/batch/advance/run")
    @ApiOperation("批次名义借款人垫付业务处理回调")
    public ResponseEntity<String> thirdBatchAdvanceRunCall(HttpServletRequest request, HttpServletResponse response) {
        return borrowRepaymentThirdBiz.thirdBatchAdvanceRunCall(request, response);
    }
}
