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
import com.gofobao.framework.tender.vo.request.VoViewReturnedMoney;
import com.gofobao.framework.tender.vo.response.VoViewBackMoney;
import com.gofobao.framework.tender.vo.response.VoViewBiddingRes;
import com.gofobao.framework.tender.vo.response.VoViewSettleRes;
import com.gofobao.framework.tender.vo.response.VoViewTenderDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by admin on 2017/6/1.
 */
@Service
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
        voInvestListReq.setType(TenderConstans.BACK_MONEY);

        List<Tender> tenderList = commonQuery(voInvestListReq);
        if (CollectionUtils.isEmpty(tenderList)) {
            return Collections.EMPTY_LIST;
        }
        //投标id 集合
        List<Long> tenderIdArray = tenderList.stream()
                .map(p -> p.getId())
                .collect(Collectors.toList());
        //标Id集合
        Set<Long> borrowIdArrray = tenderList.stream()
                .map(p -> p.getBorrowId())
                .collect(Collectors.toSet());
        //期数集合
        List<BorrowCollection> borrowCollections = borrowCollectionRepository.findByTenderIdIn(tenderIdArray);

        //标集合
        List<Borrow> borrowList = borrowRepository.findByIdIn(new ArrayList(borrowIdArrray));

        Map<Long, Borrow> borrowMap = borrowList.stream()
                .collect(Collectors.toMap(Borrow::getId, Function.identity()));

        Map<Long, List<BorrowCollection>> borrowCollectionMap = borrowCollections.stream()
                .collect(groupingBy(BorrowCollection::getTenderId));

        List<VoViewBackMoney> backMoneyList = new ArrayList<>();
        tenderList.stream().forEach(p -> {
            VoViewBackMoney voViewBackMoney = new VoViewBackMoney();
            voViewBackMoney.setMoney(NumberHelper.to2DigitString(p.getValidMoney() / 100));
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
            voViewBackMoney.setInterest(NumberHelper.to2DigitString(interest / 100));
            voViewBackMoney.setPrincipal(NumberHelper.to2DigitString(principal / 100));
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
        voInvestListReq.setType(TenderConstans.BIDDING);
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
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(validMoney / 100D, apr / 100D, borrow.getTimeLimit(), borrow.getSuccessAt());
            Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            Integer earnings = NumberHelper.toInt(StringHelper.toString(calculatorMap.get("earnings")));

            voViewBiddingRes.setExpectEarnings(NumberHelper.to2DigitString(earnings / 100));
            voViewBiddingRes.setApr(NumberHelper.to2DigitString(apr / 100));
            voViewBiddingRes.setMoney(NumberHelper.to2DigitString(validMoney / 100));
            voViewBiddingRes.setBorrowName(borrow.getName());
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
        voInvestListReq.setType(TenderConstans.SETTLE);
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
            voViewSettleRes.setMoney(NumberHelper.to2DigitString(p.getValidMoney() / 100));
            voViewSettleRes.setCloseAt(DateHelper.dateToString(borrow.getCloseAt()));
            List<BorrowCollection> borrowCollectionList = borrowCollectionMaps.get(p.getId());
            Integer principal = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToInt(s -> s.getPrincipal()).sum();
            Integer interest = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToInt(s -> s.getInterest()).sum();
            voViewSettleRes.setInterest(NumberHelper.to2DigitString(interest / 100));
            voViewSettleRes.setPrincipal(NumberHelper.to2DigitString(principal / 100));
            voViewSettleRes.setCreatedAt(DateHelper.dateToString(p.getCreatedAt()));
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
        Borrow borrow = borrowRepository.findById(tender.getBorrowId());
        item.setCreatedAt(DateHelper.dateToString(tender.getCreatedAt()));
        item.setBorrowName(borrow.getName());
        //状态
        if (tender.getState() == TenderConstans.BIDDING) {
            item.setStatus(TenderConstans.BIDDING_STR);
        }
        if (tender.getState() == TenderConstans.BACK_MONEY) {
            item.setStatus(TenderConstans.BACK_MONEY_STR);
        }
        if (tender.getState() == TenderConstans.SETTLE) {
            item.setStatus(TenderConstans.SETTLE_STR);
        }
        item.setMoney(NumberHelper.to2DigitString(tender.getValidMoney() / 100));
        item.setApr(NumberHelper.to2DigitString(borrow.getApr() / 100));
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

        if (tender.getState() == TenderConstans.BACK_MONEY || tender.getState() == TenderConstans.SETTLE) {
            List<BorrowCollection> borrowCollectionList = borrowCollectionRepository.findByTenderId(tender.getId());
            //利息
            Integer interest = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToInt(s -> s.getInterest())
                    .sum();
            //本金
            Integer principal = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToInt(s -> s.getPrincipal())
                    .sum();
            //应收利息
            Integer receivableInterest = borrowCollectionList.stream()
                    .mapToInt(s -> s.getInterest())
                    .sum();

            item.setInterest(NumberHelper.to2DigitString(interest / 100));
            item.setPrincipal(NumberHelper.to2DigitString(principal / 100));
            item.setReceivableInterest(NumberHelper.to2DigitString(receivableInterest / 100));
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
            returnedMoney.setInterest(NumberHelper.to2DigitString(p.getInterest() / 100));
            returnedMoney.setPrincipal(NumberHelper.to2DigitString(p.getPrincipal() / 100));
            returnedMoney.setCollectionMoney(NumberHelper.to2DigitString(p.getCollectionMoney() / 100));
            returnedMoney.setOrder(p.getOrder() + 1);
            returnedMoney.setLateDays(p.getLateDays());
            returnedMoney.setCollectionAt(DateHelper.dateToString(p.getCollectionAt()));
            returnedMonies.add(returnedMoney);
        });
        viewReturnedMoney.setReturnedMonies(returnedMonies);
        return Optional.ofNullable(viewReturnedMoney).orElse(null);
    }
}
