package com.gofobao.framework.repayment.controller;

import com.gofobao.framework.repayment.vo.response.VoViewRefundRes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by admin on 2017/6/2.
 */

@ApiModel("我的借款")
@RestController
@RequestMapping("/loan")
public class LoanController  {




        @ApiOperation("还款中列表")
        @GetMapping("/refund/list")
        public List<VoViewRefundRes> refundResList(){


             return  null;
        }





}
