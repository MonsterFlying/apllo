package com.gofobao.framework.tender.biz;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.tender.vo.request.VoCancelThirdTenderReq;
import com.gofobao.framework.tender.vo.request.VoCreateThirdTenderReq;
import com.gofobao.framework.tender.vo.request.VoThirdBatchCreditInvest;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Zeke on 2017/6/1.
 */
public interface TenderThirdBiz {
    ResponseEntity<VoBaseResp> createThirdTender(VoCreateThirdTenderReq voCreateThirdTenderReq);

    ResponseEntity<VoBaseResp> cancelThirdTender(VoCancelThirdTenderReq voCancelThirdTenderReq);

    /**
     * 投资人批次购买债权
     * @return
     */
    ResponseEntity<VoBaseResp> thirdBatchCreditInvest(VoThirdBatchCreditInvest voThirdBatchCreditInvest) throws Exception;

    /**
     * 投资人批次购买债权参数验证回调
     * @return
     */
    void thirdBatchCreditInvestCheckCall(HttpServletRequest request, HttpServletResponse response);

    /**
     * 投资人批次购买债权参数运行回调
     * @return
     */
    void thirdBatchCreditInvestRunCall(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
