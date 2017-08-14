package com.gofobao.framework.finance.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.vo.request.VoFinancePlanTender;
import com.gofobao.framework.finance.vo.request.VoTenderFinancePlan;
import com.gofobao.framework.finance.vo.response.VoViewFinancePlanTender;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/8/10.
 */
public interface FinancePlanBiz {

    /**
     * 理财计划匹配债权转让
     *
     * @param voFinancePlanTender
     * @return
     */
    ResponseEntity<VoViewFinancePlanTender>  financePlanTender(VoFinancePlanTender voFinancePlanTender) ;

    /**
     * 理财计划投标
     *
     * @param voTenderFinancePlan
     * @return
     */
    ResponseEntity<VoBaseResp> tenderFinancePlan(VoTenderFinancePlan voTenderFinancePlan) throws Exception;
}
