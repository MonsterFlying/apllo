package com.gofobao.framework.tender.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.InvestRepository;
import com.gofobao.framework.tender.service.InvestService;
import com.gofobao.framework.tender.vo.request.ReturnedMoney;
import com.gofobao.framework.tender.vo.request.VoDetailReq;
import com.gofobao.framework.tender.vo.request.VoInvestListReq;
import com.gofobao.framework.tender.vo.response.*;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * /**
 * Created by admin on 2017/6/1.
 */
@Slf4j
@Component
public class InvestServiceImpl implements InvestService {

    @Autowired
    private InvestRepository investRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BorrowCollectionRepository borrowCollectionRepository;

    /**
     * 回款中列表
     *
     * @param voInvestListReq
     * @return
     */
    @Override
    public List<VoViewBackMoney> backMoneyList(VoInvestListReq voInvestListReq) {


        List<Tender> tenderList = commonQuery(voInvestListReq);
        if (CollectionUtils.isEmpty(tenderList)) {
            return Collections.EMPTY_LIST;
        }
        //投标id 集合
        List<Long> tenderIdArray = tenderList.stream()
                .map(p -> p.getId())
                .collect(Collectors.toList());
        //标Id集合
        Set<Long> borrowIdArray = tenderList.stream()
                .map(p -> p.getBorrowId())
                .collect(Collectors.toSet());
        //期数集合
        List<BorrowCollection> borrowCollections = borrowCollectionRepository.findByTenderIdIn(tenderIdArray);

        //标集合
        List<Borrow> borrowList = borrowRepository.findByIdIn(new ArrayList(borrowIdArray));

        Map<Long, Borrow> borrowMap = borrowList.stream()
                .collect(Collectors.toMap(Borrow::getId, Function.identity()));

        Map<Long, List<BorrowCollection>> borrowCollectionMap = borrowCollections.stream()
                .collect(groupingBy(BorrowCollection::getTenderId));

        List<VoViewBackMoney> backMoneyList = new ArrayList<>();
        tenderList.stream().forEach(p -> {
            VoViewBackMoney voViewBackMoney = new VoViewBackMoney();
            voViewBackMoney.setMoney(StringHelper.formatMon(p.getValidMoney() / 100d));
            List<BorrowCollection> borrowCollectionList = borrowCollectionMap.get(p.getId());
            Long count = borrowCollectionList.stream().filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_NO).count();
            voViewBackMoney.setOrder(count.intValue());
            Borrow borrow = borrowMap.get(p.getBorrowId());
            voViewBackMoney.setBorrowName(borrow.getName());
            Integer principal = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_NO)
                    .mapToInt(s -> s.getPrincipal()).sum();
            Integer interest = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_NO)
                    .mapToInt(s -> s.getInterest()).sum();
            voViewBackMoney.setInterest(StringHelper.formatMon(interest / 100d));
            voViewBackMoney.setPrincipal(StringHelper.formatMon(principal / 100d));
            voViewBackMoney.setTenderId(p.getId());
            voViewBackMoney.setCreatedAt(DateHelper.dateToString(p.getCreatedAt()));
            backMoneyList.add(voViewBackMoney);
        });
        return Optional.ofNullable(backMoneyList).orElse(Collections.EMPTY_LIST);
    }

    /**
     * 投标中列表
     *
     * @param voInvestListReq
     * @return
     */
    @Override
    public List<VoViewBiddingRes> biddingList(VoInvestListReq voInvestListReq) {

        List<Tender> tenderList = commonQuery(voInvestListReq);
        if (CollectionUtils.isEmpty(tenderList)) {
            return Collections.EMPTY_LIST;
        }

        //标ID集合
        Set<Long> borrowIdArrray = tenderList.stream()
                .map(p -> p.getBorrowId())
                .collect(Collectors.toSet());

        //标集合
        List<Borrow> borrowList = borrowRepository.findByIdIn(new ArrayList(borrowIdArrray));
        Map<Long, Borrow> borrowMap = borrowList.stream()
                .collect(Collectors.toMap(Borrow::getId, Function.identity()));

        List<VoViewBiddingRes> viewBiddingResList = new ArrayList<>();
        tenderList.stream().forEach(p -> {
            VoViewBiddingRes voViewBiddingRes = new VoViewBiddingRes();
            Borrow borrow = borrowMap.get(p.getBorrowId());
            voViewBiddingRes.setTenderId(p.getId());
            voViewBiddingRes.setCreatedAt(DateHelper.dateToString(p.getCreatedAt()));
            Integer timeLimit = borrow.getTimeLimit();
            if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE) {
                voViewBiddingRes.setTimeLimit(timeLimit + BorrowContants.DAY);
            } else {
                voViewBiddingRes.setTimeLimit(timeLimit + BorrowContants.MONTH);
            }
            Integer validMoney = p.getValidMoney();
            Integer apr = borrow.getApr();

            //预期收益
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(new Double(validMoney), new Double(apr), borrow.getTimeLimit(), borrow.getSuccessAt());
            Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            Integer earnings = NumberHelper.toInt(StringHelper.toString(calculatorMap.get("earnings")));

            voViewBiddingRes.setExpectEarnings(StringHelper.formatMon(earnings / 100d));
            voViewBiddingRes.setApr(StringHelper.formatMon(apr / 100d));
            voViewBiddingRes.setMoney(StringHelper.formatMon(validMoney / 100d));
            voViewBiddingRes.setBorrowName(borrow.getName());
            voViewBiddingRes.setTenderId(p.getId());
            viewBiddingResList.add(voViewBiddingRes);
        });
        return Optional.ofNullable(viewBiddingResList).orElse(Collections.EMPTY_LIST);
    }

    /**
     * 已结清列表
     *
     * @param voInvestListReq
     * @return
     */
    @Override
    public List<VoViewSettleRes> settleList(VoInvestListReq voInvestListReq) {

        List<Tender> tenderList = commonQuery(voInvestListReq);
        if (CollectionUtils.isEmpty(tenderList)) {
            return Collections.EMPTY_LIST;
        }
        Set<Long> borrowIds = tenderList.stream()
                .map(p -> p.getBorrowId())
                .collect(Collectors.toSet());
        List<Borrow> borrowList = borrowRepository.findByIdIn(new ArrayList(borrowIds));
        Map<Long, Borrow> borrowMap = borrowList.stream()
                .collect(Collectors.toMap(Borrow::getId, Function.identity()));

        Set<Long> tenderIds = tenderList.stream()
                .map(p -> p.getId())
                .collect(Collectors.toSet());
        List<BorrowCollection> borrowCollections = borrowCollectionRepository.findByTenderIdIn(new ArrayList(tenderIds));

        Map<Long, List<BorrowCollection>> borrowCollectionMaps = borrowCollections.stream()
                .collect(groupingBy(BorrowCollection::getTenderId));

        List<VoViewSettleRes> voViewSettleResList = new ArrayList<>();
        tenderList.stream().forEach(p -> {
            VoViewSettleRes voViewSettleRes = new VoViewSettleRes();
            Borrow borrow = borrowMap.get(p.getBorrowId());
            voViewSettleRes.setBorrowName(borrow.getName());
            voViewSettleRes.setMoney(StringHelper.formatMon(p.getValidMoney() / 100d));
            voViewSettleRes.setCloseAt(DateHelper.dateToString(borrow.getCloseAt()));
            List<BorrowCollection> borrowCollectionList = borrowCollectionMaps.get(p.getId());
            Integer principal = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToInt(s -> s.getPrincipal()).sum();
            Integer interest = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToInt(s -> s.getInterest()).sum();
            voViewSettleRes.setInterest(StringHelper.formatMon(interest / 100d));
            voViewSettleRes.setPrincipal(StringHelper.formatMon(principal / 100d));
            voViewSettleRes.setCreatedAt(DateHelper.dateToString(p.getCreatedAt()));
            voViewSettleRes.setTenderId(p.getId());
            voViewSettleResList.add(voViewSettleRes);
        });
        return Optional.ofNullable(voViewSettleResList).orElse(Collections.EMPTY_LIST);
    }

    /**
     * 公共查询
     *
     * @param voInvestListReq
     * @return
     */
    private List<Tender> commonQuery(VoInvestListReq voInvestListReq) {
        Specification<Tender> specification = Specifications.<Tender>and()
                .eq("userId", voInvestListReq.getUserId())
                .eq("state", voInvestListReq.getType())
                .eq("status", TenderConstans.SUCCESS)
                .eq("transferFlag", TenderConstans.TRANSFER_NO)
                .build();
        Page<Tender> tenders = investRepository.findAll(specification,
                new PageRequest(voInvestListReq.getPageIndex(),
                        voInvestListReq.getPageSize(),
                        Sort.Direction.DESC, "createdAt"));
        List<Tender> tenderList = tenders.getContent();
        return Optional.ofNullable(tenderList).orElse(Collections.EMPTY_LIST);
    }

    /**
     * 已结清 and 回款中 详情
     *
     * @param voDetailReq
     * @return
     */
    @Override
    public VoViewTenderDetail tenderDetail(VoDetailReq voDetailReq) {
        VoViewTenderDetail item = new VoViewTenderDetail();
        Tender tender = investRepository.findByIdAndUserId(voDetailReq.getTenderId(), voDetailReq.getUserId());
        if (ObjectUtils.isEmpty(tender)) {
            return null;
        }
        Borrow borrow = borrowRepository.findOne(tender.getBorrowId());
        item.setCreatedAt(DateHelper.dateToString(tender.getCreatedAt()));
        item.setBorrowName(borrow.getName());
        //状态
        if (tender.getState() == TenderConstans.BIDDING) {
            item.setStatusStr(TenderConstans.BIDDING_STR);
            item.setSuccessAt("");
        }
        if (tender.getState() == TenderConstans.BACK_MONEY) {
            item.setSuccessAt(DateHelper.dateToString(borrow.getSuccessAt()));
            item.setStatusStr(TenderConstans.BACK_MONEY_STR);
        }
        if (tender.getState() == TenderConstans.SETTLE) {
            item.setSuccessAt(DateHelper.dateToString(borrow.getSuccessAt()));
            item.setStatusStr(TenderConstans.SETTLE_STR);
        }
        item.setStatus(tender.getState());
        item.setMoney(StringHelper.formatMon(tender.getValidMoney() / 100d));
        item.setApr(StringHelper.formatMon(borrow.getApr() / 100d));
        //期限
        if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE) {
            item.setRepayFashion(BorrowContants.REPAY_FASHION_ONCE_STR);
            item.setTimeLimit(borrow.getTimeLimit() + BorrowContants.DAY);
        } else {
            item.setTimeLimit(borrow.getTimeLimit() + BorrowContants.MONTH);
        }


        //还款方式
        if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_MONTH) {
            item.setRepayFashion(BorrowContants.REPAY_FASHION_MONTH_STR);
        }
        if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL) {
            item.setRepayFashion(BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL_STR);
        }

        Integer receivableInterest=0;
        Integer principal=0;
        Integer interest=0;

        if (tender.getState() == TenderConstans.BACK_MONEY || tender.getState() == TenderConstans.SETTLE) {
            List<BorrowCollection> borrowCollectionList = borrowCollectionRepository.findByTenderId(tender.getId());
            //利息
             interest = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToInt(s -> s.getInterest())
                    .sum();
            //本金
             principal = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToInt(s -> s.getPrincipal())
                    .sum();
            //应收利息
             receivableInterest = borrowCollectionList.stream()
                    .mapToInt(s -> s.getInterest())
                    .sum();

            item.setInterest(StringHelper.formatMon(interest / 100d));
            item.setPrincipal(StringHelper.formatMon(principal / 100d));
            item.setReceivableInterest(StringHelper.formatMon(receivableInterest / 100d));
        }
        return item;
    }

    /**
     * 回款详情
     *
     * @param voDetailReq
     * @return
     */
    @Override
    public VoViewReturnedMoney infoList(VoDetailReq voDetailReq) {
        Tender tender = investRepository.findByIdAndUserId(voDetailReq.getTenderId(), voDetailReq.getUserId());
        if (ObjectUtils.isEmpty(tender) || tender.getState() == TenderConstans.BIDDING) {
            return null;
        }
        //回款中
        Integer status = tender.getStatus() == TenderConstans.BACK_MONEY ? BorrowCollectionContants.STATUS_NO : BorrowCollectionContants.STATUS_YES;
        Specification specification = Specifications.<BorrowCollection>and()
                .eq("tenderId", tender.getId())
                .eq("status", status)
                .build();
        List<BorrowCollection> borrowCollectionList = investRepository.findAll(specification);
        if (CollectionUtils.isEmpty(borrowCollectionList)) {
            return null;
        }
        VoViewReturnedMoney viewReturnedMoney = new VoViewReturnedMoney();

        viewReturnedMoney.setOrderCount(borrowCollectionList.size());
        Integer collectionMoneySum = borrowCollectionList.stream().mapToInt(p -> p.getCollectionMoney()).sum();
        viewReturnedMoney.setCollectionMoneySum(NumberHelper.to2DigitString(collectionMoneySum));

        List<ReturnedMoney> returnedMonies = new ArrayList<>(0);
        borrowCollectionList.stream().forEach(p -> {
            ReturnedMoney returnedMoney = new ReturnedMoney();
            returnedMoney.setInterest(StringHelper.formatMon(p.getInterest() / 100d));
            returnedMoney.setPrincipal(StringHelper.formatMon(p.getPrincipal() / 100d));
            returnedMoney.setCollectionMoney(StringHelper.formatMon(p.getCollectionMoney() / 100d));
            returnedMoney.setOrder(p.getOrder() + 1);
            returnedMoney.setLateDays(p.getLateDays());
            returnedMoney.setCollectionAt(DateHelper.dateToString(p.getCollectionAt()));
            returnedMonies.add(returnedMoney);
        });
        viewReturnedMoney.setReturnedMonies(returnedMonies);
        return Optional.ofNullable(viewReturnedMoney).orElse(null);
    }
}
