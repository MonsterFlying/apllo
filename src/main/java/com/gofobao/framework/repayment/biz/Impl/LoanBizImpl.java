package com.gofobao.framework.repayment.biz.Impl;

import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.repayment.biz.LoanBiz;
import com.gofobao.framework.repayment.contants.RepaymentContants;
import com.gofobao.framework.repayment.service.LoanService;
import com.gofobao.framework.repayment.vo.request.VoDetailReq;
import com.gofobao.framework.repayment.vo.request.VoLoanListReq;
import com.gofobao.framework.repayment.vo.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2017/6/5.
 */
@Service
public class LoanBizImpl implements LoanBiz {
    @Autowired
    private LoanService loanService;

    /**
     * 还款中
     *
     * @param voLoanListReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewRefundWrapRes> refundResList(VoLoanListReq voLoanListReq) {
        voLoanListReq.setType(RepaymentContants.REFUND);
        voLoanListReq.setStatus(BorrowContants.PASS);
        try {
            Map<String, Object> resultMaps = loanService.refundResList(voLoanListReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<VoViewRefundRes> voViewRefundRes = (List<VoViewRefundRes>) resultMaps.get("refundResList");
            VoViewRefundWrapRes voViewRefundWrapRes = VoBaseResp.ok("查询成功", VoViewRefundWrapRes.class);
            voViewRefundWrapRes.setList(voViewRefundRes);
            voViewRefundWrapRes.setTotalCount(totalCount);
            return ResponseEntity.ok(voViewRefundWrapRes);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "获取查询异常", VoViewRefundWrapRes.class));
        }

    }

    /**
     * 已结清
     *
     * @param voLoanListReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewSettleWarpListRes> settleList(VoLoanListReq voLoanListReq) {
        voLoanListReq.setType(RepaymentContants.CLOSE);
        voLoanListReq.setStatus(BorrowContants.PASS);
        try {

            Map<String, Object> resultMaps = loanService.settleList(voLoanListReq);
            List<VoViewSettleRes> voViewSettleRes = (List<VoViewSettleRes>) resultMaps.get("settleList");
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());

            VoViewSettleWarpListRes viewSettleWarpListRes = VoBaseResp.ok("查询成功", VoViewSettleWarpListRes.class);
            viewSettleWarpListRes.setVoViewSettleRes(voViewSettleRes);
            viewSettleWarpListRes.setTotalCount(totalCount);
            return ResponseEntity.ok(viewSettleWarpListRes);
        } catch (Exception e) {

            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "获取查询异常", VoViewSettleWarpListRes.class));
        }

    }

    /**
     * 招标中
     *
     * @param voLoanListReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewBuddingResListWrapRes> buddingList(VoLoanListReq voLoanListReq) {
        voLoanListReq.setType(RepaymentContants.BUDING);
        voLoanListReq.setStatus(RepaymentContants.BUDING);
        try {

            Map<String, Object> resultMaps = loanService.buddingList(voLoanListReq);
            List<VoViewBuddingRes> viewBiddingRes = (List<VoViewBuddingRes>) resultMaps.get("buddingList");
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());

            VoViewBuddingResListWrapRes voViewBudingResListWrapRes = VoBaseResp.ok("查询成功", VoViewBuddingResListWrapRes.class);
            voViewBudingResListWrapRes.setViewBuddingResList(viewBiddingRes);
            voViewBudingResListWrapRes.setTotalCount(totalCount);
            return ResponseEntity.ok(voViewBudingResListWrapRes);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "获取查询异常", VoViewBuddingResListWrapRes.class));

        }

    }

    /**
     * 借款详情
     *
     * @param voDetailReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewRepaymentDetailWrapRes> repaymentDetail(VoDetailReq voDetailReq) {
        try {
            VoViewRepaymentDetail viewRepaymentDetail = loanService.repaymentDetail(voDetailReq);
            VoViewRepaymentDetailWrapRes viewRepaymentDetailWrapRes = VoBaseResp.ok("查询成功", VoViewRepaymentDetailWrapRes.class);
            viewRepaymentDetailWrapRes.setViewRepaymentDetail(viewRepaymentDetail);
            return ResponseEntity.ok(viewRepaymentDetailWrapRes);

        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "获取查询异常", VoViewRepaymentDetailWrapRes.class));
        }
    }


    /**
     * 还款列表
     *
     * @param voDetailReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewLoanInfoListWrapRes> loanList(VoDetailReq voDetailReq) {
        try {
            VoViewLoanList voViewLoanList = loanService.loanList(voDetailReq);
            VoViewLoanInfoListWrapRes viewLoanInfoListWrapRes = VoBaseResp.ok("查询成功", VoViewLoanInfoListWrapRes.class);
            viewLoanInfoListWrapRes.setVoLoanInfoList(voViewLoanList);

            return ResponseEntity.ok(viewLoanInfoListWrapRes);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "获取查询异常", VoViewLoanInfoListWrapRes.class));
        }
    }
}