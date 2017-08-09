package com.gofobao.framework.borrow.controller;

import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zeke on 2017/6/16.
 */
@RestController
@Slf4j
@Api(description = "首页标接口")
@RequestMapping("/pub/borrow")
public class BorrowThirdController {

    @Autowired
    private BorrowThirdBiz borrowThirdBiz;

    /**
     * 即信批次还款(提前结清)
     *
     * @return
     */
    @RequestMapping("/v2/third/repayall/check")
    public ResponseEntity<String> thirdBatchRepayAllCheckCall(HttpServletRequest request, HttpServletResponse response) throws Exception{
        return borrowThirdBiz.thirdBatchRepayAllCheckCall(request, response);
    }

    /**
     * 即信批次还款
     *
     * @return
     */
    @PostMapping("/v2/third/repayall/run")
    public ResponseEntity<String> thirdBatchRepayAllRunCall(HttpServletRequest request, HttpServletResponse response) {
        return borrowThirdBiz.thirdBatchRepayAllRunCall(request, response);
    }

    /**
     * 即信受托支付回调
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/v2/third/trusteepay/run")
    public ResponseEntity<String> thirdTrusteePayCall(HttpServletRequest request, HttpServletResponse response) {
        return borrowThirdBiz.thirdTrusteePayCall(request, response);
    }
}
