package com.gofobao.framework.repayment.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.vo.request.*;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zeke on 2017/6/8.
 */
public interface BorrowRepaymentThirdBiz {
    /**
     * 即信批次还款
     *
     * @param voThirdBatchRepay
     * @return
     */
    ResponseEntity<VoBaseResp> thirdBatchRepay(VoThirdBatchRepay voThirdBatchRepay);

    /**
     * 即信批次还款
     *
     * @return
     */
    void thirdBatchRepayCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次还款
     *
     * @return
     */
    void thirdBatchRepayRunCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次放款  （满标后调用）
     *
     * @param voThirdBatchLendRepay
     * @return
     */
    ResponseEntity<VoBaseResp> thirdBatchLendRepay(VoThirdBatchLendRepay voThirdBatchLendRepay);

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    void thirdBatchLendRepayCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    void thirdBatchLendRepayRunCall(HttpServletRequest request, HttpServletResponse response);
}
