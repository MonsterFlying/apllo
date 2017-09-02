package com.gofobao.framework.scheduler.controller;


import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.scheduler.FundStatisticsScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FinancialController {
    @Autowired
    FundStatisticsScheduler fundStatisticsScheduler;

    @GetMapping("pub/scheduler/{password}/{id}")
    public ResponseEntity<VoBaseResp> scheduler(
            @PathVariable(value = "password") String password,
            @PathVariable(value = "id") long id) {
        if (id < 1) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "禁止访问"));
        }

        if (!"@GOFOBAO0701WEIBO----=====".equals(password)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "禁止访问"));
        }

        fundStatisticsScheduler.scheduler(id) ;
        return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "成功"));
    }
}
