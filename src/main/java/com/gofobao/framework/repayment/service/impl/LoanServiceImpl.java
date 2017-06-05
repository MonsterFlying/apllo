package com.gofobao.framework.repayment.service.impl;

import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.repayment.contants.RepaymentContants;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.repository.BorrowRepaymentRepository;
import com.gofobao.framework.repayment.service.LoanService;
import com.gofobao.framework.repayment.vo.request.VoLoanListReq;
import com.gofobao.framework.repayment.vo.response.VoViewBudingRes;
import com.gofobao.framework.repayment.vo.response.VoViewRefundRes;
import com.gofobao.framework.repayment.vo.response.VoViewSettleRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by admin on 2017/6/2.
 */
@Service
public class LoanServiceImpl implements LoanService {
    @Autowired
    private BorrowRepaymentRepository repaymentRepository;
    @Autowired
    private BorrowRepository borrowRepository;

    /**
     * 还款中列表
     *
     * @param voLoanListReq
     * @return
     */
    @Override
    public List<VoViewRefundRes> refundResList(VoLoanListReq voLoanListReq) {
        voLoanListReq.setType(RepaymentContants.REFUND);
        voLoanListReq.setStatus(BorrowContants.PASS);

        List<Borrow> borrowList = commonQuery(voLoanListReq);

        if (CollectionUtils.isEmpty(borrowList)) {
            return null;
        }
        List<Long> borrowIds = borrowList.stream()
                .map(s -> s.getId())
                .collect(Collectors.toList());
        List<BorrowRepayment> borrowRepayments = repaymentRepository.findByBorrowIdInAndStatusIs(borrowIds, RepaymentContants.STATUS_NO);
        Map<Long, List<BorrowRepayment>> borrowRepaymentMaps = borrowRepayments.stream()
                .collect(groupingBy(BorrowRepayment::getBorrowId));

        List<VoViewRefundRes> refundRes = new ArrayList<>();
        borrowList.stream().forEach(p -> {
            VoViewRefundRes voViewRefundRes = new VoViewRefundRes();
            List<BorrowRepayment> borrowRepaymentList = borrowRepaymentMaps.get(p.getId());
            voViewRefundRes.setBorrowName(p.getName());
            voViewRefundRes.setOrder(borrowRepaymentList.size());
            voViewRefundRes.setReleaseAt(DateHelper.dateToString(p.getReleaseAt()));
            voViewRefundRes.setMoney(NumberHelper.to2DigitString(p.getMoneyYes() / 100));
            voViewRefundRes.setBorrowId(p.getId());
            Integer interest = borrowRepaymentList.stream().mapToInt(w -> w.getInterest()).sum();  //待还利息
            Integer principal = borrowRepaymentList.stream().mapToInt(w -> w.getPrincipal()).sum();  //待还本金

            voViewRefundRes.setPrincipal(NumberHelper.to2DigitString(principal / 100));
            voViewRefundRes.setInterest(NumberHelper.to2DigitString(interest / 100));
            refundRes.add(voViewRefundRes);
        });
        return Optional.ofNullable(refundRes).orElse(Collections.EMPTY_LIST);
    }

    /**
     * 已结清列表
     *
     * @param voLoanListReq
     * @return
     */
    @Override
    public List<VoViewSettleRes> settleList(VoLoanListReq voLoanListReq) {

        List<Borrow> borrowList = commonQuery(voLoanListReq);
        if (CollectionUtils.isEmpty(borrowList)) {
            return null;
        }
        List<Long> borrowIds = borrowList.stream()
                .map(s -> s.getId())
                .collect(Collectors.toList());
        List<BorrowRepayment> borrowRepayments = repaymentRepository.findByBorrowIdInAndStatusIs(borrowIds, RepaymentContants.STATUS_YES);
        Map<Long, List<BorrowRepayment>> borrowRepaymentMaps = borrowRepayments.stream()
                .collect(groupingBy(BorrowRepayment::getBorrowId));

        List<VoViewSettleRes> resArrayList = new ArrayList<>();
        borrowList.stream().forEach(p -> {
            VoViewSettleRes viewSettleRes = new VoViewSettleRes();
            List<BorrowRepayment> borrowRepaymentList = borrowRepaymentMaps.get(p.getId());
            Integer interest = borrowRepaymentList.stream().mapToInt(w -> w.getInterest()).sum();  //待还利息
            Integer principal = borrowRepaymentList.stream().mapToInt(w -> w.getPrincipal()).sum();  //待还本金
            viewSettleRes.setPrincipal(NumberHelper.to2DigitString(principal / 100));
            viewSettleRes.setInterest(NumberHelper.to2DigitString(interest / 100));
            viewSettleRes.setMoney(NumberHelper.to2DigitString(p.getMoneyYes() / 100));
            viewSettleRes.setBorrowName(p.getName());
            viewSettleRes.setBorrowId(p.getId());
            viewSettleRes.setReleaseAt(DateHelper.dateToString(p.getReleaseAt()));
            viewSettleRes.setCloseAt(DateHelper.dateToString(p.getCloseAt()));
            resArrayList.add(viewSettleRes);
        });
        return Optional.ofNullable(resArrayList).orElse(Collections.EMPTY_LIST);

    }

    /**
     * 招标中
     *
     * @param voLoanListReq
     * @return
     */
    @Override
    public List<VoViewBudingRes> budingList(VoLoanListReq voLoanListReq) {
        voLoanListReq.setType(RepaymentContants.BUDING);
        List<Borrow> borrowList = commonQuery(voLoanListReq);
        if (CollectionUtils.isEmpty(borrowList)) {
            return null;
        }

        List<VoViewBudingRes> budingResList = new ArrayList<>();
        borrowList.stream().forEach(p -> {
            VoViewBudingRes budingRes = new VoViewBudingRes();
            budingRes.setBorrowId(p.getId());
            budingRes.setBorrowName(p.getName());
            budingRes.setMoney(NumberHelper.to2DigitString(p.getMoney() / 100));
            budingRes.setApr(NumberHelper.to2DigitString(p.getApr() / 100));
            budingRes.setSpeed(NumberHelper.to2DigitString(p.getMoneyYes() / p.getMoney()));
            if (p.getRepayFashion() == 1) {
                budingRes.setTimeLimit(p.getTimeLimit() + BorrowContants.DAY);
            } else {
                budingRes.setTimeLimit(p.getTimeLimit() + BorrowContants.MONTH);
            }

        });

        return null;
    }

    private List<Borrow> commonQuery(VoLoanListReq voLoanListReq) {
        Page<Borrow> borrowPage = null;
        Sort sort;
        Pageable pageable;
        if (voLoanListReq.getType() == RepaymentContants.REFUND) { //还款中
            sort = new Sort(Sort.Direction.DESC, "closeAt");
            pageable = new PageRequest(voLoanListReq.getPageIndex(), voLoanListReq.getPageSize(), sort);
            borrowPage = repaymentRepository.findByUserIdEqAndStatusEqAndSuccessAtIsNONullAndCloseAtIsNull(
                    voLoanListReq.getUserId(),
                    voLoanListReq.getStatus(),
                    pageable);
        } else if (voLoanListReq.getType() == RepaymentContants.BUDING) { //招标中
            sort = new Sort(Sort.Direction.DESC, "releaseAt");
            pageable = new PageRequest(voLoanListReq.getPageIndex(), voLoanListReq.getPageSize(), sort);
        } else {
            sort = new Sort(Sort.Direction.DESC, "successAt");
            pageable = new PageRequest(voLoanListReq.getPageIndex(), voLoanListReq.getPageSize(), sort);
            borrowPage = repaymentRepository.findByUserIdEqAndStatusEqAndSuccessAtIsNONullAndCloseAtIsNotNull(
                    voLoanListReq.getUserId(),
                    voLoanListReq.getStatus(),
                    pageable);
        }
        return Optional.ofNullable(borrowPage.getContent()).orElse(Collections.EMPTY_LIST);

    }
}
