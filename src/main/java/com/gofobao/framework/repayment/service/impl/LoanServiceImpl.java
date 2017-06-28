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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager entityManager;


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
            voViewRefundRes.setMoney(StringHelper.formatMon(p.getMoneyYes() / 100d));
            voViewRefundRes.setBorrowId(p.getId());
            Integer interest = borrowRepaymentList.stream().mapToInt(w -> w.getInterest()).sum();  //待还利息
            Integer principal = borrowRepaymentList.stream().mapToInt(w -> w.getPrincipal()).sum();  //待还本金

            voViewRefundRes.setPrincipal(StringHelper.formatMon(principal / 100d));
            voViewRefundRes.setInterest(StringHelper.formatMon(interest / 100d));
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
            viewSettleRes.setPrincipal(StringHelper.formatMon(principal / 100d));
            viewSettleRes.setInterest(StringHelper.formatMon(interest / 100d));
            viewSettleRes.setMoney(StringHelper.formatMon(p.getMoneyYes() / 100d));
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
            budingRes.setMoney(StringHelper.formatMon(p.getMoney() / 100d));
            budingRes.setApr(StringHelper.formatMon(p.getApr() / 100d));
            budingRes.setSpeed(StringHelper.formatMon(p.getMoneyYes() /new Double( p.getMoney())));
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
        Page<Borrow> borrowPage ;
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
        repaymentDetail.setApr(StringHelper.formatMon(borrow.getApr() / 100d));
        repaymentDetail.setMoney(StringHelper.formatMon(borrow.getMoneyYes() / 100d));
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
        Integer  interest=0;
        Integer principal = 0;
        Integer receivableInterest=0;
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
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(new Double(borrow.getValidDay()), new Double(borrow.getApr()) , borrow.getTimeLimit(), borrow.getSuccessAt());
            Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            receivableInterest = NumberHelper.toInt(StringHelper.toString(calculatorMap.get("interest")));

        }
        repaymentDetail.setBorrowId(borrow.getId());
        if(borrow.getStatus()==BorrowContants.BIDDING){
            repaymentDetail.setStatus(2);
        }
        repaymentDetail.setInterest(StringHelper.formatMon(interest / 100d));
        repaymentDetail.setPrincipal(StringHelper.formatMon(principal / 100d));
        repaymentDetail.setReceivableInterest(StringHelper.formatMon(receivableInterest / 100d));
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

        VoViewLoanList voViewLoanList=new VoViewLoanList();


        Integer collectionMoney=repaymentList.stream().mapToInt(p->p.getRepayMoney()).sum();
        Integer countOrder=repaymentList.size();

        List<VoLoanInfo> voLoanInfoList=new ArrayList<>();
        repaymentList.stream().forEach(p->{
            VoLoanInfo voLoanInfo=new VoLoanInfo();
            voLoanInfo.setOrder(p.getOrder()+1);
            voLoanInfo.setPrincipal(StringHelper.formatMon(p.getPrincipal()/100d));
            voLoanInfo.setStatus(p.getStatus());
            voLoanInfo.setLateDays(p.getLateDays());
            voLoanInfo.setInterest(StringHelper.formatMon(p.getInterest()/100d));
            voLoanInfo.setRepayAt(DateHelper.dateToString(p.getRepayAt()));
            voLoanInfo.setRepayMoney(StringHelper.formatMon(p.getRepayMoney()));
            voLoanInfo.setStatusStr(p.getStatus()==RepaymentContants.STATUS_YES?RepaymentContants.STATUS_YES_STR:RepaymentContants.STATUS_NO_STR);
            voLoanInfoList.add(voLoanInfo);
        });
        voViewLoanList.setSumRepayMoney(StringHelper.formatMon(collectionMoney/100d));
        voViewLoanList.setOrderCount(countOrder);
        voViewLoanList.setVoLoanInfoList(voLoanInfoList);
        return Optional.ofNullable(voViewLoanList).orElse(null);
    }
}
