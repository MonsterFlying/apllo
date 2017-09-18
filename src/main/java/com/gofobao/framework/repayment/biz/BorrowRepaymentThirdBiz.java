package com.gofobao.framework.repayment.biz;

import com.gofobao.framework.api.model.batch_bail_repay.BatchBailRepayRunResp;
import com.gofobao.framework.api.model.batch_lend_pay.BatchLendPayRunResp;
import com.gofobao.framework.api.model.batch_repay.BatchRepayRunResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.vo.request.VoThirdBatchLendRepay;
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
     * @return
     */
    ResponseEntity<String> thirdBatchRepayCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次还款
     *
     * @return
     */
    ResponseEntity<String> thirdBatchRepayRunCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次放款  （满标后调用）
     *
     * @param voThirdBatchLendRepay
     * @return
     */
    ResponseEntity<VoBaseResp> thirdBatchLendRepay(VoThirdBatchLendRepay voThirdBatchLendRepay) throws Exception;

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    ResponseEntity<String> thirdBatchLendRepayCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 即信批次放款  （满标后调用）
     *
     * @return
     */
    ResponseEntity<String> thirdBatchLendRepayRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 批次名义借款人垫付参数检查回调
     */
    ResponseEntity<String> thirdBatchAdvanceCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 批次名义借款人垫付业务处理回调
     */
    ResponseEntity<String> thirdBatchAdvanceRunCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 处理即信批次还款
     * @param repayRunResp
     * @return
     */
    ResponseEntity<String> dealBatchRepay(BatchRepayRunResp repayRunResp);


    /**
     * 处理批次名义借款人垫付处理
     * @param batchBailRepayRunResp
     * @return
     */
    ResponseEntity<String> dealBatchAdvance(BatchBailRepayRunResp batchBailRepayRunResp);

    /**
     * 处理即信批次放款
     *
     * @param lendRepayRunResp
     * @return
     */
    ResponseEntity<String> dealBatchLendRepay(BatchLendPayRunResp lendRepayRunResp);

}
