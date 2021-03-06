package com.gofobao.framework.finance.biz;

import com.gofobao.framework.common.page.Page;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.vo.request.*;
import com.gofobao.framework.finance.vo.response.PlanDetail;
import com.gofobao.framework.finance.vo.response.PlanListWarpRes;
import com.gofobao.framework.finance.vo.response.VoViewFinanceServerPlanResp;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Created by Zeke on 2017/8/10.
 */
public interface FinancePlanBiz {

    /**
     * 理财列表
     *
     * @param page
     * @return
     */
    ResponseEntity<VoViewFinanceServerPlanResp> financeServerlist(Page page);

    /**
     * 理财计划复审
     *
     * @param voFinanceAgainVerifyTransfer
     */
    ResponseEntity<VoBaseResp> financeAgainVerifyTransfer(VoFinanceAgainVerifyTransfer voFinanceAgainVerifyTransfer) throws Exception;

    /**
     * 理财计划资金变动
     *
     * @param voFinancePlanAssetChange
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> financePlanAssetChange(VoFinancePlanAssetChange voFinancePlanAssetChange) throws Exception;

    /**
     * 理财计划匹配债权转让
     *
     * @param voFinancePlanTender
     * @return
     */
    ResponseEntity<String> financePlanTender(VoFinancePlanTender voFinancePlanTender) throws Exception;

    /**
     * 理财列表
     *
     * @param page
     * @return
     */
    ResponseEntity<PlanListWarpRes> list(Page page);


    /**
     * 理财详情
     *
     * @param id
     * @return
     */
    ResponseEntity<PlanDetail> details(Long id);


    /**
     * 理财计划投标
     *
     * @param voTenderFinancePlan
     * @return
     */
    ResponseEntity<VoBaseResp> tenderFinancePlan(VoTenderFinancePlan voTenderFinancePlan) throws Exception;

    /**
     * 理财计划回购
     *
     * @param voFinanceRepurchase
     * @return
     * @throws Exception
     */
    ResponseEntity<VoBaseResp> financeRepurchase(VoFinanceRepurchase voFinanceRepurchase) throws Exception;

    /**
     *
     * @param plan
     * @param userId
     * @return
     */
    Map<String, Object> flanContract(Long plan, Long userId);


}
