package com.gofobao.framework.repayment.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.service.LoanService;
import com.gofobao.framework.repayment.vo.request.VoLoanListReq;
import com.gofobao.framework.repayment.vo.response.VoViewRefundRes;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @ApiOperation("还款中列表")
    @GetMapping("/v2/refund/list")
    public ResponseEntity refundResList(@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        VoLoanListReq voLoanListReq = new VoLoanListReq();
        userId=901L;
        voLoanListReq.setUserId(userId);
        try {
            List<VoViewRefundRes> voViewRefundResList = loanService.refundResList(voLoanListReq);
            return ResponseEntity.ok(voViewRefundResList);
        } catch (Exception e) {
            return ResponseEntity.
                    badRequest().
                    body(VoBaseResp.error(VoBaseResp.ERROR, "获取广富币列表失败!"));
        }
    }

}
