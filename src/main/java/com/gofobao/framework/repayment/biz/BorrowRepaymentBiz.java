package com.gofobao.framework.repayment.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import org.springframework.http.ResponseEntity;

/**
 * Created by Zeke on 2017/6/6.
 */
public interface BorrowRepaymentBiz {
    /**
     * 还款
     * @param voRepayReq
     * @return
     */
    ResponseEntity<VoBaseResp> repay(VoRepayReq voRepayReq) throws Exception;
}
