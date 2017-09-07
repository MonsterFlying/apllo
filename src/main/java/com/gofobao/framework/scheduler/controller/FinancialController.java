package com.gofobao.framework.scheduler.controller;


import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.scheduler.FundStatisticsScheduler;
import com.gofobao.framework.scheduler.biz.FundStatisticsBiz;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@Slf4j
public class FinancialController {
    @Autowired
    FundStatisticsScheduler fundStatisticsScheduler;

    @Autowired
    FundStatisticsBiz fundStatisticsBiz;

    @GetMapping("/pub/scheduler/{password}/{id}")
    public ResponseEntity<VoBaseResp> scheduler(
            @PathVariable(value = "password") String password,
            @PathVariable(value = "id") long id) {
        if (id < 1) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "禁止访问"));
        }

        if (!"@GOFOBAO0701WEIBO----=====".equals(password)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "禁止访问"));
        }

        fundStatisticsScheduler.scheduler(id);
        return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "成功"));
    }


    @GetMapping("/pub/financial/{password}/{date}")
    public void financial(
            HttpServletResponse httpServletResponse,
            @PathVariable String password,
            @PathVariable String date) {
        if (!"@GOFOBAO0701WEIBO----=====".equals(password)) {
            return;
        }
        try {
            fundStatisticsBiz.downloadFundFile(httpServletResponse, date);
        } catch (Exception e) {
            log.error("资金流水", e);
            return;
        }
    }



    @GetMapping("/pub/financial/download/{password}/{userId}")
    public void downloadFinancial(
            HttpServletResponse httpServletResponse,
            HttpServletRequest httpServletRequest,
            @PathVariable String password,
            @PathVariable Long userId) {
        if (!"@GOFOBAO0701WEIBO----=====".equals(password)) {
            return;
        }
        try {
            fundStatisticsBiz.downloadOnline(httpServletResponse,httpServletRequest, userId);



        } catch (Exception e) {
            log.error("资金流水", e);
            return;
        }
    }
}
