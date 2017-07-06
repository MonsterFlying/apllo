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
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @PersistenceContext
    private EntityManager entityManager;


    /**
     * 还款中列表
     *
     * @param voLoanListReq
     * @return
     */
    @Override
    public Map<String, Object> refundResList(VoLoanListReq voLoanListReq) {
        Map<String,Object>resultMaps=Maps.newHashMap();

        Map<String,Object> borrowsMaps = commonQuery(voLoanListReq);
        List<Borrow>borrowList=(List<Borrow>) borrowsMaps.get("borrows");
        //总记录数
        resultMaps.put("totalCount",borrowsMaps.get("totalCount"));
        if (CollectionUtils.isEmpty(borrowList)) {
            resultMaps.put("refundResList",new ArrayList<>());
            return resultMaps;
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
            voViewRefundRes.setMoney(StringHelper.formatMon(p.getMoneyYes() / 100D));
            voViewRefundRes.setBorrowId(p.getId());
            Integer interest = borrowRepaymentList.stream().mapToInt(w -> w.getInterest()).sum();  //待还利息
            Integer principal = borrowRepaymentList.stream().mapToInt(w -> w.getPrincipal()).sum();  //待还本金

            voViewRefundRes.setPrincipal(StringHelper.formatMon(principal / 100D));
            voViewRefundRes.setInterest(StringHelper.formatMon(interest / 100D));
            refundRes.add(voViewRefundRes);
        });
        resultMaps.put("refundResList",refundRes);
        return resultMaps;
    }

    /**
     * 已结清列表
     *
     * @param voLoanListReq
     * @return
     */
    @Override
    public Map<String, Object> settleList(VoLoanListReq voLoanListReq) {
        Map<String,Object>resultMap=Maps.newHashMap();

        Map<String,Object> borrowsMaps = commonQuery(voLoanListReq);
        resultMap.put("totalCount",borrowsMaps.get("totalCount"));


        List<Borrow>borrowList=(List<Borrow>) borrowsMaps.get("borrows");
        if (CollectionUtils.isEmpty(borrowList)) {
            resultMap.put("settleList",new ArrayList<>());
            return resultMap;
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
            viewSettleRes.setPrincipal(StringHelper.formatMon(principal / 100D));
            viewSettleRes.setInterest(StringHelper.formatMon(interest / 100D));
            viewSettleRes.setMoney(StringHelper.formatMon(p.getMoneyYes() / 100D));
            viewSettleRes.setBorrowName(p.getName());
            viewSettleRes.setBorrowId(p.getId());
            viewSettleRes.setReleaseAt(DateHelper.dateToString(p.getReleaseAt()));
            viewSettleRes.setCloseAt(DateHelper.dateToString(p.getCloseAt()));
            resArrayList.add(viewSettleRes);
        });
        resultMap.put("settleList",resArrayList);

        return resultMap;

    }

    /**
     * 招标中
     *
     * @param voLoanListReq
     * @return
     */
    @Override
    public Map<String, Object> buddingList(VoLoanListReq voLoanListReq) {
        Map<String,Object>resultMaps=Maps.newHashMap();

        Map<String,Object> borrowsMaps = commonQuery(voLoanListReq);
        List<Borrow>borrowList=(List<Borrow>) borrowsMaps.get("borrows");


        resultMaps.put("totalCount",borrowsMaps.get("totalCount"));
        if (CollectionUtils.isEmpty(borrowList)) {
            resultMaps.put("buddingList",new ArrayList<>());
            return resultMaps;
        }
        List<VoViewBuddingRes> budingResList = new ArrayList<>();
        borrowList.stream().forEach(p -> {
            VoViewBuddingRes budingRes = new VoViewBuddingRes();
            budingRes.setBorrowId(p.getId());
            budingRes.setBorrowName(p.getName());
            budingRes.setMoney(StringHelper.formatMon(p.getMoney() / 100D));
            budingRes.setApr(StringHelper.formatMon(p.getApr() / 100D));
            budingRes.setSpeed(StringHelper.formatMon(p.getMoneyYes() / new Double(p.getMoney())));
            if (p.getRepayFashion() == 1) {
                budingRes.setTimeLimit(p.getTimeLimit() + BorrowContants.DAY);
            } else {
                budingRes.setTimeLimit(p.getTimeLimit() + BorrowContants.MONTH);
            }
            budingResList.add(budingRes);
        });
        resultMaps.put("buddingList",budingResList);
        return resultMaps;
    }


    /**
     * 拆分查询
     *
     * @param voLoanListReq
     * @return
     */
    private Map<String, Object> commonQuery(VoLoanListReq voLoanListReq) {
        Map<String, Object> resultMaps = Maps.newHashMap();

        Page<Borrow> borrowPage;
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
            borrowPage = loanRepository.findByUserIdAndStatusIsAndVerifyAtIsNotNull(
                    voLoanListReq.getUserId(),
                    voLoanListReq.getStatus(),
                    pageable);
        } else {
            sort = new Sort(Sort.Direction.DESC, "successAt");  //已结清
            pageable = new PageRequest(voLoanListReq.getPageIndex(), voLoanListReq.getPageSize(), sort);
            borrowPage = loanRepository.findByUserIdAndStatusIsAndSuccessAtIsNotNullAndCloseAtIsNotNullAndTenderIdIsNull(
                    voLoanListReq.getUserId(),
                    voLoanListReq.getStatus(),
                    pageable);
        }
        Long totalCount = borrowPage.getTotalElements();
        resultMaps.put("totalCount", totalCount);
        List<Borrow> borrowList = borrowPage.getContent();
        if (CollectionUtils.isEmpty(borrowList)) {
            resultMaps.put("borrows", new ArrayList<>());
        } else {
            resultMaps.put("borrows", borrowList);
        }
        return resultMaps;

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
        repaymentDetail.setApr(StringHelper.formatMon(borrow.getApr() / 100D));
        repaymentDetail.setMoney(StringHelper.formatMon(borrow.getMoneyYes() / 100D));
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
        Integer interest = 0;
        Integer principal = 0;
        Integer receivableInterest = 0;
        if (borrow.getStatus() == BorrowContants.PASS) {
            List<BorrowRepayment> borrowRepayments = repaymentRepository.findByBorrowId(borrow.getId());
            //统计还款中
            Long count = borrowRepayments.stream().filter(p -> p.getStatus() == RepaymentContants.STATUS_NO).mapToLong(w -> w.getId()).count();
            if (count > 0) {
                interest = borrowRepayments.stream().filter(p -> p.getStatus() == RepaymentContants.STATUS_NO).mapToInt(w -> w.getInterest()).sum();
                principal = borrowRepayments.stream().filter(p -> p.getStatus() == RepaymentContants.STATUS_NO).mapToInt(w -> w.getPrincipal()).sum();
                repaymentDetail.setStatus(RepaymentContants.STATUS_NO);
            } else {   //以还清
                interest = borrowRepayments.stream().filter(p -> p.getStatus() == RepaymentContants.STATUS_YES).mapToInt(w -> w.getInterest()).sum();
                principal = borrowRepayments.stream().filter(p -> p.getStatus() == RepaymentContants.STATUS_YES).mapToInt(w -> w.getPrincipal()).sum();
                repaymentDetail.setStatus(RepaymentContants.STATUS_YES);
            }
            //预期收益
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(new Double(borrow.getValidDay()), new Double(borrow.getApr()), borrow.getTimeLimit(), borrow.getSuccessAt());
            Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            receivableInterest = NumberHelper.toInt(StringHelper.toString(calculatorMap.get("interest")));

        }
        repaymentDetail.setBorrowId(borrow.getId());
        if (borrow.getStatus() == BorrowContants.BIDDING) {
            repaymentDetail.setStatus(2);
        }
        repaymentDetail.setInterest(StringHelper.formatMon(interest / 100D));
        repaymentDetail.setPrincipal(StringHelper.formatMon(principal / 100D));
        repaymentDetail.setReceivableInterest(StringHelper.formatMon(receivableInterest / 100D));
        if (borrow.getRepayFashion() == 1) {
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


        Integer collectionMoney = repaymentList.stream().mapToInt(p -> p.getRepayMoney()).sum();
        Integer countOrder = repaymentList.size();

        List<VoLoanInfo> voLoanInfoList = new ArrayList<>();
        repaymentList.stream().forEach(p -> {
            VoLoanInfo voLoanInfo = new VoLoanInfo();
            voLoanInfo.setOrder(p.getOrder() + 1);
            voLoanInfo.setPrincipal(StringHelper.formatMon(p.getPrincipal() / 100D));
            voLoanInfo.setStatus(p.getStatus());
            voLoanInfo.setLateDays(p.getLateDays());
            voLoanInfo.setInterest(StringHelper.formatMon(p.getInterest() / 100D));
            voLoanInfo.setRepayAt(DateHelper.dateToString(p.getRepayAt()));
            voLoanInfo.setRepayMoney(StringHelper.formatMon(p.getRepayMoney() / 100D));
            voLoanInfo.setStatusStr(p.getStatus() == RepaymentContants.STATUS_YES ? RepaymentContants.STATUS_YES_STR : RepaymentContants.STATUS_NO_STR);
            voLoanInfoList.add(voLoanInfo);
        });
        voViewLoanList.setSumRepayMoney(StringHelper.formatMon(collectionMoney / 100D));
        voViewLoanList.setOrderCount(countOrder);
        voViewLoanList.setVoLoanInfoList(voLoanInfoList);
        return Optional.ofNullable(voViewLoanList).orElse(null);
    }
}
