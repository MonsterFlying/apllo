package com.gofobao.framework.repayment.service;

import com.gofobao.framework.repayment.vo.request.VoDetailReq;
import com.gofobao.framework.repayment.vo.request.VoLoanListReq;
import com.gofobao.framework.repayment.vo.request.VoStatisticsReq;
import com.gofobao.framework.repayment.vo.response.VoViewLoanList;
import com.gofobao.framework.repayment.vo.response.VoViewRepaymentDetail;

import java.util.Map;

/**
 * Created by admin on 2017/6/2.
 */
public interface LoanService {

    /**
     * 还款中列表
     * @param voLoanListReq
     * @return
     */
    Map<String, Object> refundResList(VoLoanListReq voLoanListReq);


    /**
     * 已结清
     * @param voLoanListReq
     * @return
     */
    Map<String, Object> settleList(VoLoanListReq voLoanListReq);


    /**
     * 招标中
     * @param voLoanListReq
     * @return
     */
    Map<String, Object> buddingList(VoLoanListReq voLoanListReq);


    /**
     * 待复审
     * @param voLoanListReq
     * @return
     */
    Map<String,Object>rechecking(VoLoanListReq voLoanListReq);


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

    /**
     * PC :借款统计
     * @param voStatisticsReq
     * @return
     */
    Map<String,Object> statistics(VoStatisticsReq voStatisticsReq);

}
