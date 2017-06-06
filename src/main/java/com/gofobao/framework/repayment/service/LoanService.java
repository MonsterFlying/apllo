package com.gofobao.framework.repayment.service;

import com.gofobao.framework.repayment.vo.request.VoDetailReq;
import com.gofobao.framework.repayment.vo.request.VoLoanListReq;
import com.gofobao.framework.repayment.vo.response.*;


import java.util.List;

/**
 * Created by admin on 2017/6/2.
 */
public interface LoanService {

    /**
     * 还款中列表
     * @param voLoanListReq
     * @return
     */
    List<VoViewRefundRes> refundResList(VoLoanListReq voLoanListReq);


    /**
     * 已结清
     * @param voLoanListReq
     * @return
     */
    List<VoViewSettleRes> settleList(VoLoanListReq voLoanListReq);


    /**
     * 招标中
     * @param voLoanListReq
     * @return
     */
    List<VoViewBuddingRes> buddingList(VoLoanListReq voLoanListReq);


    /**
     * 还款方式
     * @param voDetailReq
     * @return
     */
    VoViewRepaymentDetail repaymentDetail(VoDetailReq voDetailReq);


    /**
     * 还款列表
     * @param voDetailReq
     * @return
     */
    VoViewLoanList loanList(VoDetailReq voDetailReq);


}
