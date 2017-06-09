package com.gofobao.framework.repayment.controller;

import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListResWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailWarpRes;
import com.gofobao.framework.repayment.biz.BorrowRepaymentThirdBiz;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.vo.request.VoInfoReq;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by admin on 2017/6/1.
 */
@RestController
@Api(description="还款计划")
@RequestMapping("/pub/repayment")
public class BorrowRepaymentThirdContorller {

    @Autowired
    private BorrowRepaymentThirdBiz borrowRepaymentThirdBiz;

    @RequestMapping("/v2/third/batch/lendrepay/check")
    @ApiOperation("批次放款参数检查通知")
    public void thirdLendRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        borrowRepaymentThirdBiz.thirdBatchLendRepayCheckCall(request, response);
    }

    @RequestMapping("/v2/third/batch/lendrepay/run")
    @ApiOperation("批次放款运行结果通知")
    public void thirdLendRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
        borrowRepaymentThirdBiz.thirdBatchLendRepayRunCall(request, response);
    }

    @RequestMapping("/v2/third/batch/repay/check")
    @ApiOperation("批次还款参数检查通知")
    public void thirdRepayCheckCall(HttpServletRequest request, HttpServletResponse response) {
        borrowRepaymentThirdBiz.thirdBatchRepayCheckCall(request, response);
    }

    @RequestMapping("/v2/third/batch/repay/run")
    @ApiOperation("批次还款参数检查通知")
    public void thirdRepayRunCall(HttpServletRequest request, HttpServletResponse response) {
        borrowRepaymentThirdBiz.thirdBatchRepayRunCall(request, response);
    }
}
