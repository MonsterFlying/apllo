package com.gofobao.framework.tender.biz;

import com.gofobao.framework.api.model.batch_credit_invest.BatchCreditInvestRunCall;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.vo.request.VoCancelThirdTenderReq;
import com.gofobao.framework.tender.vo.request.VoCreateThirdTenderReq;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zeke on 2017/6/1.
 */
public interface TenderThirdBiz {
    ResponseEntity<VoBaseResp> createThirdTender(VoCreateThirdTenderReq voCreateThirdTenderReq);

    ResponseEntity<VoBaseResp> cancelThirdTender(VoCancelThirdTenderReq voCancelThirdTenderReq);

    /**
     * 理财计划批次购买债权参数验证回调
     *
     * @return
     */
    ResponseEntity<String> thirdBatchCreditInvestFinanceCheckCall(HttpServletRequest request, HttpServletResponse response);


    /**
     * 理财计划批次购买债权参数运行回调
     *
     * @return
     */
    ResponseEntity<String> thirdBatchCreditInvestFinanceRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 处理批次购买债权转让
     *
     * @param batchCreditInvestRunCall
     * @return
     */
    ResponseEntity<String> dealBatchCreditInvest(BatchCreditInvestRunCall batchCreditInvestRunCall);

    /**
     * 投资人批次购买债权参数验证回调
     *
     * @return
     */
    ResponseEntity<String> thirdBatchCreditInvestCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 投资人批次购买债权参数运行回调
     *
     * @return
     */
    ResponseEntity<String> thirdBatchCreditInvestRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 投资人批次结束债权参数验证回调
     *
     * @return
     */
    void thirdBatchCreditEndCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 投资人批次结束债权参数运行回调
     *
     * @return
     */
    ResponseEntity<String> thirdBatchCreditEndRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
