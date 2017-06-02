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
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.InvestRepository;
import com.gofobao.framework.tender.service.InvestService;
import com.gofobao.framework.tender.vo.request.VoInvestListReq;
import com.gofobao.framework.tender.vo.response.VoViewBackMoney;
import com.gofobao.framework.tender.vo.response.VoViewBiddingRes;
import com.gofobao.framework.tender.vo.response.VoViewSettleRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 * Created by admin on 2017/6/1.
 */
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
            voViewBiddingRes.setApr(NumberHelper.to2DigitString(borrow.getApr()));
            voViewBiddingRes.setMoney(NumberHelper.to2DigitString(p.getValidMoney() / 100));
            voViewBiddingRes.setBorrowName(borrow.getName());
            viewBiddingResList.add(voViewBiddingRes);
        });
        return Optional.ofNullable(viewBiddingResList).orElse(Collections.EMPTY_LIST);
    }

    /**
     * 已结清列表
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
            Integer principal=borrowCollectionList.stream()
                    .filter(w->w.getStatus()==BorrowCollectionContants.STATUS_YES)
                    .mapToInt(s->s.getPrincipal()).sum();
            Integer interest=borrowCollectionList.stream()
                    .filter(w->w.getStatus()==BorrowCollectionContants.STATUS_YES)
                    .mapToInt(s->s.getInterest()).sum();
            voViewSettleRes.setInterest(NumberHelper.to2DigitString(interest/100));
            voViewSettleRes.setPrincipal(NumberHelper.to2DigitString(principal/100));
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










}
