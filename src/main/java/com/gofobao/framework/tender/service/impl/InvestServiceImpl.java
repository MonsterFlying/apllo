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
import com.google.common.collect.Maps;
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
    public Map<String, Object> backMoneyList(VoInvestListReq voInvestListReq) {
        Map<String, Object> resultMaps = commonQuery(voInvestListReq);
        List<Tender> tenderList = (List<Tender>) resultMaps.get("tenderList");
        if (CollectionUtils.isEmpty(tenderList)) {
            resultMaps.put("backMoneyList", new ArrayList<>(0));
            return resultMaps;
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
            voViewBackMoney.setMoney(StringHelper.formatMon(p.getValidMoney() / 100D));
            List<BorrowCollection> borrowCollectionList = borrowCollectionMap.get(p.getId());
            Borrow borrow = borrowMap.get(p.getBorrowId());
            voViewBackMoney.setBorrowName(borrow.getName());
            List<BorrowCollection> tempCollection = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_NO)
                    .collect(Collectors.toList());
            Long count = borrowCollectionList.stream().map(s -> s.getId()).count();
            voViewBackMoney.setOrder(count.intValue());
            //待收本金
            long principal = tempCollection.stream()
                    .mapToLong(s -> s.getPrincipal()).sum();
            //待收利息
            long interest = tempCollection.stream()
                    .mapToLong(s -> s.getInterest()).sum();
            //待收本息
            long collectionMoney = tempCollection.stream()
                    .mapToLong(w -> w.getCollectionMoney()).sum();

            voViewBackMoney.setCollectionMoney(StringHelper.formatMon(collectionMoney / 100D));
            voViewBackMoney.setInterest(StringHelper.formatMon(interest / 100D));
            voViewBackMoney.setPrincipal(StringHelper.formatMon(principal / 100D));
            voViewBackMoney.setTenderId(p.getId());
            voViewBackMoney.setCreatedAt(DateHelper.dateToString(p.getCreatedAt()));
            backMoneyList.add(voViewBackMoney);
        });
        resultMaps.put("backMoneyList", backMoneyList);
        return resultMaps;
    }

    /**
     * 投标中列表
     *
     * @param voInvestListReq
     * @return
     */
    @Override
    public Map<String, Object> biddingList(VoInvestListReq voInvestListReq) {
        Map<String, Object> resultMaps = commonQuery(voInvestListReq);
        List<Tender> tenderList = (List<Tender>) resultMaps.get("tenderList");
        if (CollectionUtils.isEmpty(tenderList)) {
            resultMaps.put("biddingResList", new ArrayList<>(0));
            return resultMaps;
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
            Double aDouble = borrow.getValidDay().doubleValue() / borrow.getMoney().doubleValue();
            voViewBiddingRes.setSpend(new Double(StringHelper.formatDouble(aDouble, false)));
            voViewBiddingRes.setCreatedAt(DateHelper.dateToString(p.getCreatedAt()));
            Integer timeLimit = borrow.getTimeLimit();
            if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE) {
                voViewBiddingRes.setTimeLimit(timeLimit + BorrowContants.DAY);
            } else {
                voViewBiddingRes.setTimeLimit(timeLimit + BorrowContants.MONTH);
            }
            Long validMoney = p.getValidMoney();
            Integer apr = borrow.getApr();

            //预期收益
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(new Double(validMoney), new Double(apr), borrow.getTimeLimit(), borrow.getSuccessAt());
            Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            Integer earnings = NumberHelper.toInt(StringHelper.toString(calculatorMap.get("earnings")));

            voViewBiddingRes.setExpectEarnings(StringHelper.formatMon(earnings / 100D));
            voViewBiddingRes.setApr(StringHelper.formatMon(apr / 100D));
            voViewBiddingRes.setMoney(StringHelper.formatMon(validMoney / 100D));
            voViewBiddingRes.setBorrowName(borrow.getName());
            voViewBiddingRes.setTenderId(p.getId());

            viewBiddingResList.add(voViewBiddingRes);
        });
        resultMaps.put("biddingResList", viewBiddingResList);
        return resultMaps;
    }

    /**
     * 已结清列表
     *
     * @param voInvestListReq
     * @return
     */
    @Override
    public Map<String, Object> settleList(VoInvestListReq voInvestListReq) {
        Map<String, Object> resultMaps = commonQuery(voInvestListReq);
        List<Tender> tenderList = (List<Tender>) resultMaps.get("tenderList");

        if (CollectionUtils.isEmpty(tenderList)) {
            resultMaps.put("settleResList", new ArrayList<>(0));
            return resultMaps;
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
            voViewSettleRes.setMoney(StringHelper.formatMon(p.getValidMoney() / 100D));
            voViewSettleRes.setCloseAt(DateHelper.dateToString(borrow.getCloseAt()));
            List<BorrowCollection> borrowCollectionList = borrowCollectionMaps.get(p.getId());
            List<BorrowCollection> borrowCollections1 = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .collect(Collectors.toList());
            long interest = borrowCollections1.stream()
                    .mapToLong(s -> s.getInterest()).sum();
            long principal = borrowCollections1.stream().
                    mapToLong(s -> s.getPrincipal()).sum();
            long collectionMoneyYes = borrowCollections1.stream()
                    .mapToLong(s -> s.getCollectionMoneyYes()).sum();
            voViewSettleRes.setInterest(StringHelper.formatMon(interest / 100D));
            voViewSettleRes.setPrincipal(StringHelper.formatMon(principal / 100D));
            voViewSettleRes.setCreatedAt(DateHelper.dateToString(p.getCreatedAt()));
            voViewSettleRes.setCollectionMoneyYes(StringHelper.formatMon(collectionMoneyYes / 100D));
            voViewSettleRes.setTenderId(p.getId());
            voViewSettleRes.setRemark("正常结清");
            voViewSettleResList.add(voViewSettleRes);
        });
        resultMaps.put("settleResList", voViewSettleResList);
        return resultMaps;
    }

    /**
     * 公共查询
     *
     * @param voInvestListReq
     * @return
     */
    private Map<String, Object> commonQuery(VoInvestListReq voInvestListReq) {
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
        Long totalCount = tenders.getTotalElements();
        Map<String, Object> resultMaps = Maps.newHashMap();
        resultMaps.put("totalCount", totalCount);
        resultMaps.put("tenderList", tenderList);

        return resultMaps;
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
        item.setMoney(StringHelper.formatMon(tender.getValidMoney() / 100D));
        item.setApr(StringHelper.formatMon(borrow.getApr() / 100D));
        //期限
        if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE) {
            item.setRepayFashion(BorrowContants.REPAY_FASHION_ONCE_STR);
            item.setTimeLimit(borrow.getTimeLimit() + BorrowContants.DAY);
        } else {
            item.setTimeLimit(borrow.getTimeLimit() + BorrowContants.MONTH);
        }

        item.setBorrowId(borrow.getId());
        item.setTenderId(tender.getId());
        //还款方式
        if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_MONTH) {
            item.setRepayFashion(BorrowContants.REPAY_FASHION_MONTH_STR);
        }
        if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL) {
            item.setRepayFashion(BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL_STR);
        }

        long receivableInterest = 0;
        long principal = 0;
        long interest = 0;

        if (tender.getState() == TenderConstans.BACK_MONEY || tender.getState() == TenderConstans.SETTLE) {
            List<BorrowCollection> borrowCollectionList = borrowCollectionRepository.findByTenderId(tender.getId());
            //利息
            interest = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToLong(s -> s.getInterest())
                    .sum();
            //本金
            principal = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToLong(s -> s.getPrincipal())
                    .sum();
            //应收利息
            receivableInterest = borrowCollectionList.stream()
                    .mapToLong(s -> s.getInterest())
                    .sum();

            item.setInterest(StringHelper.formatMon(interest / 100D));
            item.setPrincipal(StringHelper.formatMon(principal / 100D));
            item.setReceivableInterest(StringHelper.formatMon(receivableInterest / 100D));
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
        Specification specification = Specifications.<BorrowCollection>and()
                .eq("tenderId", tender.getId())
                .eq("userId", voDetailReq.getUserId())
                .build();
        List<BorrowCollection> borrowCollectionList = borrowCollectionRepository.findAll(specification);
        if (CollectionUtils.isEmpty(borrowCollectionList)) {
            return null;
        }
        VoViewReturnedMoney viewReturnedMoney = new VoViewReturnedMoney();

        viewReturnedMoney.setOrderCount(borrowCollectionList.size());
        long collectionMoneySum = borrowCollectionList.stream().mapToLong(p -> p.getCollectionMoney()).sum();
        viewReturnedMoney.setCollectionMoneySum(NumberHelper.to2DigitString(collectionMoneySum));

        List<ReturnedMoney> returnedMonies = new ArrayList<>(0);
        borrowCollectionList.stream().forEach(p -> {
            ReturnedMoney returnedMoney = new ReturnedMoney();
            returnedMoney.setInterest(StringHelper.formatMon(p.getInterest() / 100D));
            returnedMoney.setPrincipal(StringHelper.formatMon(p.getPrincipal() / 100D));
            returnedMoney.setCollectionMoney(StringHelper.formatMon(p.getCollectionMoney() / 100D));
            returnedMoney.setOrder(p.getOrder() + 1);
            returnedMoney.setLateDays(p.getLateDays());
            returnedMoney.setCollectionAt(DateHelper.dateToString(p.getCollectionAt()));
            returnedMoney.setStatus(p.getStatus());
            returnedMonies.add(returnedMoney);
        });
        viewReturnedMoney.setReturnedMonies(returnedMonies);
        return Optional.ofNullable(viewReturnedMoney).orElse(null);
    }
}
