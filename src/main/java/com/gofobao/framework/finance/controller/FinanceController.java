package com.gofobao.framework.finance.controller;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.FinancePlanBiz;
import com.gofobao.framework.finance.biz.FinancePlanBuyerBiz;
import com.gofobao.framework.finance.vo.request.VoFinanceAgainVerifyTransfer;
import com.gofobao.framework.finance.vo.request.VoTenderFinancePlan;
import com.gofobao.framework.finance.vo.response.*;
import com.gofobao.framework.helper.ThymeleafHelper;
import com.gofobao.framework.security.contants.SecurityContants;
import com.gofobao.framework.security.helper.JwtTokenHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * Created by Zeke on 2017/8/10.
 */
@RestController
@Api(description = "理财")
@RequestMapping("")
@Slf4j
public class FinanceController {

    @Autowired
    private FinancePlanBiz financePlanBiz;

    @Autowired
    private FinancePlanBuyerBiz financePlanBuyerBiz;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${jwt.prefix}")
    private String prefix;

    @Autowired
    private ThymeleafHelper thymeleafHelper;

    /**
     * 理财计划复审
     *
     * @param voFinanceAgainVerifyTransfer
     */
    @ApiOperation("理财计划复审")
    @PostMapping("/pub/finance/pub/again/verify")
    public ResponseEntity<VoBaseResp> financeAgainVerifyTransfer(@Valid @ModelAttribute VoFinanceAgainVerifyTransfer voFinanceAgainVerifyTransfer) {
        return financePlanBiz.financeAgainVerifyTransfer(voFinanceAgainVerifyTransfer);
    }

    @ApiOperation("理财列表")
    @GetMapping("/pub/finance/plan/list/{pageIndex}/{pageSize}")
    public ResponseEntity<PlanListWarpRes> list(@PathVariable Integer pageIndex, @PathVariable Integer pageSize) {
        Page page = new Page();
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return financePlanBiz.list(page);
    }

    @ApiOperation("金服理财理财列表")
    @GetMapping("/pub/finance/server/plan/list/{pageIndex}/{pageSize}")
    public ResponseEntity<VoViewFinanceServerPlanResp> financeServerPlanList(@PathVariable Integer pageIndex, @PathVariable Integer pageSize) {
        Page page = new Page();
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return financePlanBiz.financeServerlist(page);
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

    @ApiOperation(value = "标合同")
    @GetMapping(value = "/pub/finance/plan/flanContract/{planId}")
    public ResponseEntity<String> flanContract(@PathVariable Long planId, HttpServletRequest request) throws Exception {
        Long userId = 0L;
        String authToken = request.getHeader(this.tokenHeader);
        if (!StringUtils.isEmpty(authToken) && (authToken.contains(prefix))) {
            authToken = authToken.substring(7);
        }
        String username = jwtTokenHelper.getUsernameFromToken(authToken);
        if (!StringUtils.isEmpty(username)) {
            userId = jwtTokenHelper.getUserIdFromToken(authToken);
        }

        Map<String, Object> paramMaps = financePlanBiz.flanContract(planId, userId);
        String content = "";
        try {
            content = thymeleafHelper.build("licai/contract", paramMaps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(content);
    }

    /**
     * 购买理财计划
     *
     * @param voTenderFinancePlan
     * @return
     */
    @ApiOperation("购买理财计划")
    @PostMapping("/finance/v2/tender/finance/plan")
    public ResponseEntity<VoBaseResp> tenderFinancePlan(@Valid @ModelAttribute VoTenderFinancePlan voTenderFinancePlan, @ApiIgnore @RequestAttribute(SecurityContants.USERID_KEY) Long userId) {
        try {
            voTenderFinancePlan.setUserId(userId);
            return financePlanBiz.tenderFinancePlan(voTenderFinancePlan);
        } catch (Exception e) {
            log.error("购买理财计划异常:%s", e);
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "系统开小差了，请稍后再试!", VoViewFinancePlanTender.class));
        }
    }
}
