package com.gofobao.framework.borrow.biz;

import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryResp;
import com.gofobao.framework.borrow.vo.request.*;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchRepay;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zeke on 2017/6/1.
 */
public interface BorrowThirdBiz {
    ResponseEntity<VoBaseResp> createThirdBorrow(VoCreateThirdBorrowReq voCreateThirdBorrowReq);

    ResponseEntity<VoBaseResp> cancelThirdBorrow(VoCancelThirdBorrow voCancelThirdBorrow);

    DebtDetailsQueryResp queryThirdBorrowList(VoQueryThirdBorrowList voQueryThirdBorrowList);

    /**
     * 即信批次还款(提前结清)
     *
     * @param voRepayAllReq
     * @return
     */
    ResponseEntity<VoBaseResp> thirdBatchRepayAll(VoRepayAllReq voRepayAllReq);

    /**
     * 即信批次还款(提前结清)
     *
     * @return
     */
    void thirdBatchRepayAllCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次还款
     *
     * @return
     */
    void thirdBatchRepayAllRunCall(HttpServletRequest request, HttpServletResponse response);
}
