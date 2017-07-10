package com.gofobao.framework.repayment.biz;

import com.gofobao.framework.repayment.vo.request.VoDetailReq;
import com.gofobao.framework.repayment.vo.request.VoLoanListReq;
import com.gofobao.framework.repayment.vo.request.VoStatisticsReq;
import com.gofobao.framework.repayment.vo.response.*;
import com.gofobao.framework.repayment.vo.response.pc.VoViewLoanStatisticsWarpRes;
import org.springframework.http.ResponseEntity;

/**
 * Created by admin on 2017/6/5.
 */
public interface LoanBiz {




    /**
     * 还款中列表
     * @param voLoanListReq
     * @return
     */
    ResponseEntity<VoViewRefundWrapRes> refundResList(VoLoanListReq voLoanListReq);


    /**
     * 已结清
     * @param voLoanListReq
     * @return
     */
    ResponseEntity<VoViewSettleWarpListRes> settleList(VoLoanListReq voLoanListReq);


    /**
     * 招标中
     * @param voLoanListReq
     * @return
     */
    ResponseEntity<VoViewBuddingResListWrapRes> buddingList(VoLoanListReq voLoanListReq);


    /**
     * 还款方式
     * @param voDetailReq
     * @return
     */
    ResponseEntity<VoViewRepaymentDetailWrapRes> repaymentDetail(VoDetailReq voDetailReq);


    /**
     * 还款列表
     * @param voDetailReq
     * @return
     */
    ResponseEntity<VoViewLoanInfoListWrapRes> loanList(VoDetailReq voDetailReq);


    /**
     * 借款统计
     * @param voStatisticsReq
     * @return
     */
    ResponseEntity<VoViewLoanStatisticsWarpRes>repaymentStatistics(VoStatisticsReq voStatisticsReq);

}
