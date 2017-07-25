package com.gofobao.framework.borrow.biz;

import com.gofobao.framework.api.model.debt_details_query.DebtDetailsQueryResponse;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.vo.request.*;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.member.vo.response.VoHtmlResp;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zeke on 2017/6/1.
 */
public interface BorrowThirdBiz {
    ResponseEntity<VoBaseResp> createThirdBorrow(VoCreateThirdBorrowReq voCreateThirdBorrowReq);

    ResponseEntity<VoBaseResp> cancelThirdBorrow(VoCancelThirdBorrow voCancelThirdBorrow);

    DebtDetailsQueryResponse queryThirdBorrowList(VoQueryThirdBorrowList voQueryThirdBorrowList);


    /**
     * 查询当前标的在存管系统登记的状态
     *
     * @param borrow
     * @return
     */
    boolean registerBorrrowConditionCheck(Borrow borrow);

    /**
     * 即信批次还款(提前结清)
     *
     * @param voRepayAllReq
     * @return
     */
    ResponseEntity<VoBaseResp> thirdBatchRepayAll(VoRepayAllReq voRepayAllReq) throws Exception;

    /**
     * 即信批次还款(提前结清)
     *
     * @return
     */
    ResponseEntity<String> thirdBatchRepayAllCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次还款
     *
     * @return
     */
    ResponseEntity<String> thirdBatchRepayAllRunCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信受托支付
     *
     * @param voThirdTrusteePayReq
     * @param httpServletRequest
     * @return
     */
    ResponseEntity<VoHtmlResp> thirdTrusteePay(VoThirdTrusteePayReq voThirdTrusteePayReq, HttpServletRequest httpServletRequest);

    /**
     * 即信受托支付回调
     *
     * @param request
     * @param response
     */
    ResponseEntity<String> thirdTrusteePayCall(HttpServletRequest request, HttpServletResponse response);
}
