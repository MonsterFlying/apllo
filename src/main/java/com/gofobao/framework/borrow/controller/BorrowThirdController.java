package com.gofobao.framework.borrow.controller;

import com.gofobao.framework.borrow.biz.BorrowThirdBiz;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    @RequestMapping("/third/repayall/check")
    public void thirdBatchRepayAllCheckCall(HttpServletRequest request, HttpServletResponse response) {
        borrowThirdBiz.thirdBatchRepayAllCheckCall(request, response);
    }

    /**
     * 即信批次还款
     *
     * @return
     */
    @RequestMapping("/third/repayall/run")
    public void thirdBatchRepayAllRunCall(HttpServletRequest request, HttpServletResponse response) {
        borrowThirdBiz.thirdBatchRepayAllRunCall(request, response);
    }
}
