package com.gofobao.framework.repayment.biz;

import com.gofobao.framework.repayment.vo.request.VoDetailReq;
import com.gofobao.framework.repayment.vo.request.VoLoanListReq;
import com.gofobao.framework.repayment.vo.response.VoViewRefundRes;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Created by admin on 2017/6/5.
 */
public interface LoanBiz {




    /**
     * 还款中列表
     * @param voLoanListReq
     * @return
     */
    ResponseEntity<List<VoViewRefundRes>> refundResList(VoLoanListReq voLoanListReq);


    /**
     * 已结清
     * @param voLoanListReq
     * @return
     */
    ResponseEntity settleList(VoLoanListReq voLoanListReq);


    /**
     * 招标中
     * @param voLoanListReq
     * @return
     */
    ResponseEntity buddingList(VoLoanListReq voLoanListReq);


    /**
     * 还款方式
     * @param voDetailReq
     * @return
     */
    ResponseEntity repaymentDetail(VoDetailReq voDetailReq);


    /**
     * 还款列表
     * @param voDetailReq
     * @return
     */
    ResponseEntity loanList(VoDetailReq voDetailReq);



}
