package com.gofobao.framework.finance.controller;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.FinancePlanBiz;
import com.gofobao.framework.finance.entity.FinancePlan;
import com.gofobao.framework.finance.vo.request.VoTenderFinancePlan;
import com.gofobao.framework.finance.vo.response.VoViewFinancePlanTender;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Zeke on 2017/8/10.
 */
@RestController
@RequestMapping("/tender")
public class FinanceController {

    @Autowired
    private FinancePlanBiz financePlanBiz;

    /**
     * 购买理财计划
     *
     * @param voTenderFinancePlan
     * @return
     */
    @ApiOperation("购买理财计划")
    @PostMapping("/v2/tender/finance/plan")
    public ResponseEntity<VoBaseResp> tenderFinancePlan(VoTenderFinancePlan voTenderFinancePlan) {
        try {
            return financePlanBiz.tenderFinancePlan(voTenderFinancePlan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍后再试!", VoViewFinancePlanTender.class));
        }
    }
}
