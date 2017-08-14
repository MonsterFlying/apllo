package com.gofobao.framework.finance.controller.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.FinancePlanBiz;
import com.gofobao.framework.finance.entity.FinancePlan;
import com.gofobao.framework.finance.vo.request.VoFinancePlanTender;
import com.gofobao.framework.finance.vo.response.VoViewFinancePlanTender;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/8/10.
 */
@RestController
@RequestMapping("/tender/pc")
@Api(description = "理财计划功能模块")
public class WebFinanceController {

    @Autowired
    private FinancePlanBiz financePlanBiz;

    /**
     * 理财计划投标
     *
     * @param voFinancePlanTender
     * @return
     */
    @ApiOperation("理财计划匹配债权转让")
    @PostMapping("/v2/pub/finance/plan/tender")
    public ResponseEntity<VoViewFinancePlanTender> financePlanTender(@Valid VoFinancePlanTender voFinancePlanTender) {
        try {
            return financePlanBiz.financePlanTender(voFinancePlanTender);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍后再试!", VoViewFinancePlanTender.class));
        }
    }
}
