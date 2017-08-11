package com.gofobao.framework.finance.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.vo.request.VoFinancePlanTender;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/8/10.
 */
public interface FinancePlanBiz {
    /**
     * 理财计划投标
     *
     * @param voFinancePlanTender
     * @return
     */
    ResponseEntity<VoBaseResp> financePlanTender(VoFinancePlanTender voFinancePlanTender);
}
