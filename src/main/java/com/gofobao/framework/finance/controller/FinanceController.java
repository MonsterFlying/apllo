package com.gofobao.framework.finance.controller;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.FinancePlanBiz;
import com.gofobao.framework.finance.biz.FinancePlanBuyerBiz;
import com.gofobao.framework.finance.vo.request.VoTenderFinancePlan;
import com.gofobao.framework.finance.vo.response.PlanBuyUserListWarpRes;
import com.gofobao.framework.finance.vo.response.PlanDetail;
import com.gofobao.framework.finance.vo.response.PlanListWarpRes;
import com.gofobao.framework.finance.vo.response.VoViewFinancePlanTender;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Zeke on 2017/8/10.
 */
@RestController
@Api(description = "理财")
@RequestMapping("")
public class FinanceController {

    @Autowired
    private FinancePlanBiz financePlanBiz;

    @Autowired
    private FinancePlanBuyerBiz financePlanBuyerBiz;

    @ApiOperation("理财列表")
    @GetMapping("/pub/finance/plan/list/{pageIndex}/{pageSize}")
    public ResponseEntity<PlanListWarpRes> list(@PathVariable Integer pageIndex, @PathVariable Integer pageSize) {
        Page page = new Page();
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return financePlanBiz.list(page);
    }

    @ApiOperation("理财标详情")
    @GetMapping("/pub/finance/plan/info/{id}")
    public ResponseEntity<PlanDetail> info(@PathVariable Long id) {
        return financePlanBiz.details(id);
    }


    @ApiOperation("投资记录")
    @GetMapping("/pub/finance/plan/tender/list/logs/{id}")
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
    @PostMapping("finance/v2/tender/plan")
    public ResponseEntity<VoBaseResp> tenderFinancePlan(VoTenderFinancePlan voTenderFinancePlan) {
        try {
            return financePlanBiz.tenderFinancePlan(voTenderFinancePlan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍后再试!", VoViewFinancePlanTender.class));
        }
    }
}
