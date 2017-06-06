package com.gofobao.framework.repayment.service.impl;

import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.repayment.contants.RepaymentContants;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.repository.BorrowRepaymentRepository;
import com.gofobao.framework.repayment.repository.LoanRepository;
import com.gofobao.framework.repayment.service.LoanService;
import com.gofobao.framework.repayment.vo.request.VoDetailReq;
import com.gofobao.framework.repayment.vo.request.VoLoanListReq;
import com.gofobao.framework.repayment.vo.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by admin on 2017/6/2.
 */
@Component
public class LoanServiceImpl implements LoanService {
    @Autowired
    private BorrowRepaymentRepository repaymentRepository;

    @Autowired
    private LoanRepository loanRepository;

    /**
     * 还款中列表
     *
     * @param voLoanListReq
     * @return
     */
    @Override
    public List<VoViewRefundRes> refundResList(VoLoanListReq voLoanListReq) {

        List<Borrow> borrowList = commonQuery(voLoanListReq);
        if (CollectionUtils.isEmpty(borrowList)) {
            return Collections.EMPTY_LIST;
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
            return Collections.EMPTY_LIST;
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
    public List<VoViewBuddingRes> buddingList(VoLoanListReq voLoanListReq) {

        List<Borrow> borrowList = commonQuery(voLoanListReq);
        if (CollectionUtils.isEmpty(borrowList)) {
            return Collections.EMPTY_LIST;
        }
        List<VoViewBuddingRes> budingResList = new ArrayList<>();
        borrowList.stream().forEach(p -> {
            VoViewBuddingRes budingRes = new VoViewBuddingRes();
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
            budingResList.add(budingRes);
        });
        return Optional.ofNullable(budingResList).orElse(Collections.EMPTY_LIST);
    }


    /**
     * 拆分查询
     *
     * @param voLoanListReq
     * @return
     */
    private List<Borrow> commonQuery(VoLoanListReq voLoanListReq) {
        Page<Borrow> borrowPage = null;
        Sort sort;
        Pageable pageable;
        if (voLoanListReq.getType() == RepaymentContants.REFUND) { //还款中
            sort = new Sort(Sort.Direction.DESC, "closeAt");
            pageable = new PageRequest(voLoanListReq.getPageIndex(), voLoanListReq.getPageSize(), sort);
            borrowPage = loanRepository.findByUserIdAndStatusIsAndSuccessAtIsNotNullAndCloseAtIsNullAndTenderIdIsNull(
                    voLoanListReq.getUserId(),
                    voLoanListReq.getStatus(),
                    pageable);
        } else if (voLoanListReq.getType() == RepaymentContants.BUDING) { //招标中
            sort = new Sort(Sort.Direction.DESC, "releaseAt");
            pageable = new PageRequest(voLoanListReq.getPageIndex(), voLoanListReq.getPageSize(), sort);
            borrowPage = loanRepository.findByUserIdAndStatusIs(
                    voLoanListReq.getUserId(),
                    voLoanListReq.getStatus(),
                    pageable);
        } else {
            sort = new Sort(Sort.Direction.DESC, "successAt");
            pageable = new PageRequest(voLoanListReq.getPageIndex(), voLoanListReq.getPageSize(), sort);
            borrowPage = loanRepository.findByUserIdAndStatusIsAndSuccessAtIsNotNullAndCloseAtIsNotNullAndTenderIdIsNull(
                    voLoanListReq.getUserId(),
                    voLoanListReq.getStatus(),
                    pageable);
        }
        return Optional.ofNullable(borrowPage.getContent()).orElse(Collections.EMPTY_LIST);

    }

    /**
     * 还款详情
     *
     * @param voDetailReq
     * @return
     */
    @Override
    public VoViewRepaymentDetail repaymentDetail(VoDetailReq voDetailReq) {

        Borrow borrow = loanRepository.findOne(voDetailReq.getBorrowId());
        if (ObjectUtils.isEmpty(borrow) || !borrow.getUserId().equals(voDetailReq.getUserId())) {
            return null;
        }
        VoViewRepaymentDetail repaymentDetail = new VoViewRepaymentDetail();
        repaymentDetail.setBorrowName(borrow.getName());
        repaymentDetail.setApr(NumberHelper.to2DigitString(borrow.getApr() / 100));
        repaymentDetail.setMoney(NumberHelper.to2DigitString(borrow.getMoneyYes() / 100));
        repaymentDetail.setSuccessAt(DateHelper.dateToString(borrow.getSuccessAt()));
        repaymentDetail.setCreatedAt(DateHelper.dateToString(borrow.getCreatedAt()));
        String repayFashion = "";
        if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE) {
            repayFashion = BorrowContants.REPAY_FASHION_ONCE_STR;
        }
        if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL) {
            repayFashion = BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL_STR;
        }
        if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_MONTH) {
            repayFashion = BorrowContants.REPAY_FASHION_MONTH_STR;
        }
        repaymentDetail.setRepayFashion(repayFashion);
        if (borrow.getStatus() != BorrowContants.BIDDING) {
            List<BorrowRepayment> borrowRepayments = repaymentRepository.findByBorrowId(borrow.getId());
            //统计还款中
            Long count = borrowRepayments.stream().filter(p -> p.getStatus() == RepaymentContants.STATUS_NO).mapToLong(w -> w.getId()).count();
            String statusStr = "";
            Integer interest = 0;
            Integer principal = 0;
            if (count > 0) {
                statusStr = RepaymentContants.STATUS_NO_STR;
                interest = borrowRepayments.stream().filter(p -> p.getStatus() == RepaymentContants.STATUS_NO).mapToInt(w -> w.getInterest()).sum();
                principal = borrowRepayments.stream().filter(p -> p.getStatus() == RepaymentContants.STATUS_NO).mapToInt(w -> w.getPrincipal()).sum();
            } else {   //以还清
                statusStr = RepaymentContants.STATUS_YES_STR;
                interest = borrowRepayments.stream().filter(p -> p.getStatus() == RepaymentContants.STATUS_YES).mapToInt(w -> w.getInterest()).sum();
                principal = borrowRepayments.stream().filter(p -> p.getStatus() == RepaymentContants.STATUS_YES).mapToInt(w -> w.getPrincipal()).sum();
            }
            repaymentDetail.setInterest(NumberHelper.to2DigitString(interest / 100));
            repaymentDetail.setPrincipal(NumberHelper.to2DigitString(principal / 100));
            repaymentDetail.setStatusStr(statusStr);

            //预期收益
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(borrow.getValidDay() / 100D, borrow.getApr() / 100D, borrow.getTimeLimit(), borrow.getSuccessAt());
            Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            Integer receivableInterest = NumberHelper.toInt(StringHelper.toString(calculatorMap.get("interest")));
            repaymentDetail.setReceivableInterest(NumberHelper.to2DigitString(receivableInterest / 100));

        }


        if (borrow.getTimeLimit() == 1) {
            repaymentDetail.setTimeLimit(borrow.getTimeLimit() + BorrowContants.DAY);
        } else {
            repaymentDetail.setTimeLimit(borrow.getTimeLimit() + BorrowContants.MONTH);
        }

        return repaymentDetail;
    }


    /**
     * @param voDetailReq
     * @return
     */
    @Override
    public VoViewLoanList loanList(VoDetailReq voDetailReq) {
        Borrow borrow = loanRepository.findOne(voDetailReq.getBorrowId());
        if (ObjectUtils.isEmpty(borrow) || !borrow.getUserId().equals(voDetailReq.getUserId())) {
            return null;
        }
        List<BorrowRepayment> repaymentList = repaymentRepository.findByBorrowId(borrow.getId());
        if (CollectionUtils.isEmpty(repaymentList)) {
            return null;
        }
        VoViewLoanList voViewLoanList = new VoViewLoanList();
        Long countId = repaymentList.stream().filter(p -> p.getStatus() == RepaymentContants.STATUS_NO).mapToLong(w -> w.getId()).count();
        List<BorrowRepayment> borrowRepayments = new ArrayList<>(0);
        String statusStr;
        if (countId > 0) {  //还款中
            borrowRepayments = repaymentList.stream().filter(p -> p.getStatus() == RepaymentContants.STATUS_NO).collect(Collectors.toList());
            voViewLoanList.setOrderCount(countId.intValue());
            statusStr = RepaymentContants.STATUS_NO_STR;
        } else { //此标已结清
            borrowRepayments = repaymentList;
            voViewLoanList.setOrderCount(repaymentList.size());
            statusStr = RepaymentContants.STATUS_YES_STR;
        }
        Integer repayMoney = borrowRepayments.stream().mapToInt(w -> w.getRepayMoney()).sum();
        voViewLoanList.setSumRepayMoney(NumberHelper.to2DigitString(repayMoney / 100));  //总金额
        List<VoLoanInfo> voLoanInfoList=new ArrayList<>();
        borrowRepayments.stream().forEach(p -> {
            VoLoanInfo loanInfo = new VoLoanInfo();
            loanInfo.setOrder(p.getOrder() + 1);
            loanInfo.setStatusStr(statusStr);
            loanInfo.setRepayMoney(NumberHelper.to2DigitString(p.getRepayMoney() / 100));
            Date repayAt = new Date();
            if (countId > 0) {
                repayAt = p.getRepayAt();
                loanInfo.setStatus(RepaymentContants.STATUS_NO);
            } else {
                repayAt = p.getRepayAtYes();
                loanInfo.setStatus(RepaymentContants.STATUS_YES);
            }
            loanInfo.setRepayAt(DateHelper.dateToString(repayAt));
            loanInfo.setLateDays(p.getLateDays());
            loanInfo.setInterest(NumberHelper.to2DigitString(p.getInterest()/100));
            loanInfo.setPrincipal(NumberHelper.to2DigitString(p.getPrincipal()/100));
            voLoanInfoList.add(loanInfo);
        });
        voViewLoanList.setVoLoanInfoList(voLoanInfoList);

        return Optional.ofNullable(voViewLoanList).orElse(null);
    }
}
