package com.gofobao.framework.repayment.biz.Impl;

import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.repayment.biz.LoanBiz;
import com.gofobao.framework.repayment.contants.RepaymentContants;
import com.gofobao.framework.repayment.service.LoanService;
import com.gofobao.framework.repayment.vo.request.VoDetailReq;
import com.gofobao.framework.repayment.vo.request.VoLoanListReq;
import com.gofobao.framework.repayment.vo.request.VoStatisticsReq;
import com.gofobao.framework.repayment.vo.response.*;
import com.gofobao.framework.repayment.vo.response.pc.LoanStatistics;
import com.gofobao.framework.repayment.vo.response.pc.VoViewLoanStatisticsWarpRes;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
        } catch (Throwable e) {
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
            List<VoViewSettleRespc> voViewSettleRes = (List<VoViewSettleRespc>) resultMaps.get("settleList");
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());

            VoViewSettleWarpListRes viewSettleWarpListRes = VoBaseResp.ok("查询成功", VoViewSettleWarpListRes.class);
            viewSettleWarpListRes.setSettleRes(voViewSettleRes);
            viewSettleWarpListRes.setTotalCount(totalCount);
            return ResponseEntity.ok(viewSettleWarpListRes);
        } catch (Throwable e) {

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
        } catch (Throwable e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "获取查询异常", VoViewBuddingResListWrapRes.class));

        }

    }

    @Override
    public ResponseEntity<VoViewBuddingResListWrapRes> rechecking(VoLoanListReq voLoanListReq) {
        VoViewBuddingResListWrapRes wrapRes = VoBaseResp.ok("", VoViewBuddingResListWrapRes.class);
        Map<String, Object> resultMaps = loanService.rechecking(voLoanListReq);
        List<Borrow> borrows = (List<Borrow>) resultMaps.get("borrows");
        Integer totalCount = (Integer) resultMaps.get("totalCount");
        resultMaps.put("totalCount", totalCount);
        if (CollectionUtils.isEmpty(borrows)) {
            wrapRes.setViewBuddingResList(Lists.newArrayList());
            return ResponseEntity.ok(wrapRes);
        }
        List<VoViewBuddingRes> resArrayList = Lists.newArrayList();
        borrows.forEach(borrow -> {
            VoViewBuddingRes item = new VoViewBuddingRes();
            item.setApr(StringHelper.formatMon(borrow.getApr() / 100D));
            item.setBorrowName(borrow.getName());
            item.setSpeed("1");
            item.setBorrowId(borrow.getId());
            item.setMoney(StringHelper.formatMon(borrow.getMoney() / 100D));
            item.setTimeLimit(borrow.getRepayFashion() == 1 ? borrow.getTimeLimit() + BorrowContants.DAY : borrow.getTimeLimit() + BorrowContants.MONTH);
            item.setBorrowId(borrow.getId());
            item.setCancel(false);
            resArrayList.add(item);
        });
        wrapRes.setViewBuddingResList(resArrayList);
        return ResponseEntity.ok(wrapRes);
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

        } catch (Throwable e) {
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
        } catch (Throwable e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "获取查询异常", VoViewLoanInfoListWrapRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewLoanStatisticsWarpRes> repaymentStatistics(VoStatisticsReq voStatisticsReq) {
        try {
            Map<String, Object> resultMaps = loanService.statistics(voStatisticsReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<LoanStatistics> borrowRepayments = (List<LoanStatistics>) resultMaps.get("repayments");
            VoViewLoanStatisticsWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewLoanStatisticsWarpRes.class);
            warpRes.setTotalCount(totalCount);
            warpRes.setStatisticss(borrowRepayments);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "获取查询异常", VoViewLoanStatisticsWarpRes.class));
        }
    }
}