package com.gofobao.framework.repayment.biz.Impl;

import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.repayment.biz.LoanBiz;
import com.gofobao.framework.repayment.contants.RepaymentContants;
import com.gofobao.framework.repayment.service.LoanService;
import com.gofobao.framework.repayment.vo.request.VoDetailReq;
import com.gofobao.framework.repayment.vo.request.VoLoanListReq;
import com.gofobao.framework.repayment.vo.response.VoViewRefundRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by admin on 2017/6/5.
 */
@Service
public class LoanBizImpl implements LoanBiz {

    @Autowired
    private LoanService loanService;

    @Override
    public ResponseEntity<List<VoViewRefundRes>> refundResList(VoLoanListReq voLoanListReq) {
        voLoanListReq.setType(RepaymentContants.REFUND);
        voLoanListReq.setStatus(BorrowContants.PASS);
        try {
            List<VoViewRefundRes>  voViewRefundRes = loanService.refundResList(voLoanListReq);
            return new ResponseEntity<>(voViewRefundRes,HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity settleList(VoLoanListReq voLoanListReq) {
        return null;
    }

    @Override
    public ResponseEntity buddingList(VoLoanListReq voLoanListReq) {
        return null;
    }

    @Override
    public ResponseEntity repaymentDetail(VoDetailReq voDetailReq) {
        return null;
    }

    @Override
    public ResponseEntity loanList(VoDetailReq voDetailReq) {
        return null;
    }
}
