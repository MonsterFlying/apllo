package com.gofobao.framework.finance.controller;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.finance.biz.FinancePlanBiz;
import com.gofobao.framework.finance.biz.FinancePlanBuyerBiz;
import com.gofobao.framework.finance.vo.response.PlanBuyUserListWarpRes;
import com.gofobao.framework.finance.vo.response.PlanDetail;
import com.gofobao.framework.finance.vo.response.PlanListWarpRes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Zeke on 2017/8/10.
 */
@RestController
@RequestMapping("/pub/finance")
@Api(description = "理财")
public class FinanceController {

    @Autowired
    private FinancePlanBiz financePlanBiz;

    @Autowired
    private FinancePlanBuyerBiz financePlanBuyerBiz;

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


}
