package com.gofobao.framework.finance.controller;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.FinancePlanBiz;
import com.gofobao.framework.finance.biz.FinancePlanBuyerBiz;
import com.gofobao.framework.finance.vo.request.VoFinanceAgainVerifyTransfer;
import com.gofobao.framework.finance.vo.request.VoFinancePlanAssetChange;
import com.gofobao.framework.finance.vo.request.VoTenderFinancePlan;
import com.gofobao.framework.finance.vo.response.PlanBuyUserListWarpRes;
import com.gofobao.framework.finance.vo.response.PlanDetail;
import com.gofobao.framework.finance.vo.response.PlanListWarpRes;
import com.gofobao.framework.finance.vo.response.VoViewFinancePlanTender;
import com.gofobao.framework.security.contants.SecurityContants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;

/**
 * Created by Zeke on 2017/8/10.
 */
@RestController
@Api(description = "理财")
@RequestMapping("/pub/finance")
public class FinanceController {

    @Autowired
    private FinancePlanBiz financePlanBiz;

    @Autowired
    private FinancePlanBuyerBiz financePlanBuyerBiz;

    /**
     * 理财计划复审
     *
     * @param voFinanceAgainVerifyTransfer
     */
    @ApiOperation("理财计划复审")
    @GetMapping("/pub/again/verify")
    public ResponseEntity<VoBaseResp> financeAgainVerifyTransfer(@Valid @ModelAttribute VoFinanceAgainVerifyTransfer voFinanceAgainVerifyTransfer) {
        return financePlanBiz.financeAgainVerifyTransfer(voFinanceAgainVerifyTransfer);
    }

    @ApiOperation("理财列表")
    @GetMapping("/plan/list/{pageIndex}/{pageSize}")
    public ResponseEntity<PlanListWarpRes> list(@PathVariable Integer pageIndex, @PathVariable Integer pageSize) {
        Page page = new Page();
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return financePlanBiz.list(page);
    }

    @ApiOperation("理财标详情")
    @GetMapping("/plan/info/{id}")
    public ResponseEntity<PlanDetail> info(@PathVariable Long id) {
        return financePlanBiz.details(id);
    }


    @ApiOperation("投资记录")
    @GetMapping("/plan/tender/list/logs/{id}")
    public ResponseEntity<PlanBuyUserListWarpRes> buyUserList(@PathVariable Long id) {
        return financePlanBuyerBiz.buyUserList(id);
    }

    /**
     * 购买理财计划
     *
     * @param voTenderFinancePlan
     * @return
     */
    @ApiOperation("购买理财计划")
    @PostMapping("/v2/tender/finance/plan")
    public ResponseEntity<VoBaseResp> tenderFinancePlan(@Valid @ModelAttribute VoTenderFinancePlan voTenderFinancePlan,@ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        try {
            voTenderFinancePlan.setUserId(userId);
            return financePlanBiz.tenderFinancePlan(voTenderFinancePlan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍后再试!", VoViewFinancePlanTender.class));
        }
    }
}
