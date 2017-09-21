package com.gofobao.framework.tender.controller;

import com.gofobao.framework.tender.biz.TenderThirdBiz;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zeke on 2017/6/9.
 */
@Api(description = "投标相关接口")
@RequestMapping("/pub/tender")
@RestController
@Slf4j
public class TenderThirdController {
    @Autowired
    private TenderThirdBiz tenderThirdBiz;

    /**
     * 理财计划批次购买债权运行回调
     *
     * @return
     */
    @ApiOperation("理财计划批次购买债权运行回调")
    @RequestMapping("/v2/third/batch/finance/creditinvest/run")
    public ResponseEntity<String> thirdFinanceBatchCreditInvestRunCall(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("理财计划批量债权购买回调触发");
            return tenderThirdBiz.thirdBatchCreditInvestRunCall(request, response);
        } catch (Exception e) {
            log.error("理财计划批量债权购买回调失败", e);
            return ResponseEntity.ok("error");
        }
    }

    /**
     * 理财计划批次购买债权参数验证回调
     *
     * @return
     */
    @ApiOperation("理财计划批次购买债权参数验证回调")
    @RequestMapping("/v2/third/batch/finance/creditinvest/check")
    public ResponseEntity<String> thirdFinanceBatchCreditInvestCheckCall(HttpServletRequest request, HttpServletResponse response) {
        return tenderThirdBiz.thirdBatchCreditInvestCheckCall(request, response);
    }

    /**
     * 投资人批次购买债权运行回调
     *
     * @return
     */
    @ApiOperation("投资人批次购买债权运行回调")
    @RequestMapping("/v2/third/batch/creditinvest/run")
    public ResponseEntity<String> thirdBatchCreditInvestRunCall(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("批量债权购买回调触发");
            return tenderThirdBiz.thirdBatchCreditInvestRunCall(request, response);
        } catch (Exception e) {
            log.error("批量债权购买回调失败", e);
            return ResponseEntity.ok("error");
        }
    }

    /**
     * 投资人批次购买债权参数验证回调
     *
     * @return
     */
    @ApiOperation("投资人批次购买债权参数验证回调")
    @RequestMapping("/v2/third/batch/creditinvest/check")
    public ResponseEntity<String> thirdBatchCreditInvestCheckCall(HttpServletRequest request, HttpServletResponse response) {
        return tenderThirdBiz.thirdBatchCreditInvestCheckCall(request, response);
    }



    /**
     * 投资人批次结束债权运行回调
     *
     * @return
     */
    @ApiOperation("投资人批次结束债权运行回调")
    @RequestMapping("/v2/third/batch/creditend/run")
    public ResponseEntity<String> thirdBatchCreditEndRunCall(HttpServletRequest request, HttpServletResponse response) {
        try {
            return tenderThirdBiz.thirdBatchCreditEndRunCall(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("error");
        }
    }

    /**
     * 投资人批次结束债权参数验证回调
     *
     * @return
     */
    @ApiOperation("投资人批次结束债权参数验证回调")
    @RequestMapping("/v2/third/batch/creditend/check")
    public void thirdBatchCreditEndCheckCall(HttpServletRequest request, HttpServletResponse response) {
        tenderThirdBiz.thirdBatchCreditEndCheckCall(request, response);
    }
}
