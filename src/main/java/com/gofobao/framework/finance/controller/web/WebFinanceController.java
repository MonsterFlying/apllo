package com.gofobao.framework.finance.controller.web;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.FinancePlanBiz;
import com.gofobao.framework.finance.vo.request.VoFinancePlanAssetChange;
import com.gofobao.framework.finance.vo.request.VoFinancePlanTender;
import com.gofobao.framework.finance.vo.request.VoFinanceRepurchase;
import com.gofobao.framework.finance.vo.response.VoViewFinancePlanTender;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/8/10.
 */
@RestController
@RequestMapping("/plan/pc")
@Api(description = "理财计划功能模块")
@Slf4j
public class WebFinanceController {

    @Autowired
    private FinancePlanBiz financePlanBiz;


    /**
     * 理财计划回购
     *
     * @param voFinanceRepurchase
     * @return
     * @throws Exception
     */
    @ApiOperation("理财计划匹配债权转让")
    @PostMapping("/v2/pub/finance/plan/repurchase")
    public ResponseEntity<VoBaseResp> financeRepurchase(VoFinanceRepurchase voFinanceRepurchase) {
        try {
            return financePlanBiz.financeRepurchase(voFinanceRepurchase);
        } catch (Exception e) {
            log.error("理财计划匹配债权转让 异常：",e);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "回购失败!"));
        }
    }

    /**
     * 理财计划投标
     *
     * @param voFinancePlanTender
     * @return
     */
    @ApiOperation("理财计划匹配债权转让")
    @PostMapping("/v2/pub/finance/plan/tender")
    public ResponseEntity<String> financePlanTender(@Valid @ModelAttribute VoFinancePlanTender voFinancePlanTender) {
        try {
            return financePlanBiz.financePlanTender(voFinancePlanTender);
        } catch (Exception e) {
            log.error("理财计划匹配债权转让 异常：",e);
            return ResponseEntity.badRequest().body("匹配失败!");
        }
    }

    /**
     * 理财计划资金变动
     *
     * @param voFinancePlanAssetChange
     * @return
     */
    @ApiOperation("理财计划资金变动")
    @PostMapping("/v2/pub/finance/asset/change")
    public ResponseEntity<VoBaseResp> financePlanAssetChange(@Valid @ModelAttribute VoFinancePlanAssetChange voFinancePlanAssetChange) {
        try {
            return financePlanBiz.financePlanAssetChange(voFinancePlanAssetChange);
        } catch (Exception e) {

            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍后再试!", VoViewFinancePlanTender.class));
        }
    }
}
