package com.gofobao.framework.finance.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.contants.BorrowContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.finance.entity.FinancePlan;
import com.gofobao.framework.finance.entity.FinancePlanBuyer;
import com.gofobao.framework.finance.entity.FinancePlanCollection;
import com.gofobao.framework.finance.service.FinancePlanBuyerService;
import com.gofobao.framework.finance.service.FinancePlanCollectionService;
import com.gofobao.framework.finance.service.FinancePlanService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.helper.project.BorrowCalculatorHelper;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.contants.TransferContants;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.entity.Transfer;
import com.gofobao.framework.tender.repository.InvestRepository;
import com.gofobao.framework.finance.service.FinanceInvestService;
import com.gofobao.framework.tender.vo.request.ReturnedMoney;
import com.gofobao.framework.tender.vo.request.VoDetailReq;
import com.gofobao.framework.tender.vo.request.VoInvestListReq;
import com.gofobao.framework.tender.vo.response.*;
import com.google.common.collect.Maps;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
public class FinanceInvestServiceImpl implements FinanceInvestService {

    @Autowired
    private InvestRepository investRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BorrowCollectionRepository borrowCollectionRepository;

    @Autowired
    private FinancePlanService financePlanService;

    @Autowired
    private FinancePlanBuyerService financePlanBuyerService;

    @Autowired
    private FinancePlanCollectionService financePlanCollectionService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 回款中列表
     *
     * @param voInvestListReq
     * @return
     */
    @Override
    public Map<String, Object> backMoneyList(VoInvestListReq voInvestListReq) {
        Map<String, Object> resultMaps = commonQuery(voInvestListReq);
        //理财计划购买记录
        List<FinancePlanBuyer> financePlanBuyerList = (List<FinancePlanBuyer>) resultMaps.get("financePlanBuyerList");
        if (CollectionUtils.isEmpty(financePlanBuyerList)) {
            resultMaps.put("settleResList", new ArrayList<>(0));
            return resultMaps;
        }

        //理财计划id集合
        Set<Long> planIds = financePlanBuyerList.stream()
                .map(p -> p.getPlanId())
                .collect(Collectors.toSet());
        Specification<FinancePlan> fps = Specifications
                .<FinancePlan>and()
                .in("id", planIds.toArray())
                .build();
        List<FinancePlan> financePlanList = financePlanService.findList(fps);
        Map<Long, FinancePlan> financePlanMap = financePlanList.stream()
                .collect(Collectors.toMap(FinancePlan::getId, Function.identity()));
        //期数集合
        //查询理财计划回款记录
        Set<Long> buyerIds = financePlanBuyerList.stream().map(FinancePlanBuyer::getId).collect(Collectors.toSet());
        Specification<FinancePlanCollection> fpcs = Specifications
                .<FinancePlanCollection>and()
                .in("buyerId", buyerIds.toArray())
                .build();
        List<FinancePlanCollection> financePlanCollectionList = financePlanCollectionService.findList(fpcs);
        Map<Long, List<FinancePlanCollection>> financePlanCollectionMap = financePlanCollectionList.stream()
                .collect(groupingBy(FinancePlanCollection::getBuyerId));

        List<VoViewFinanceBackMoney> viewFinanceBackMoneyList = new ArrayList<>();
        financePlanBuyerList.stream().forEach(p -> {
            List<FinancePlanCollection> financePlanCollections = financePlanCollectionMap.get(p.getId());
            FinancePlan financePlan = financePlanMap.get(p.getPlanId());


            VoViewFinanceBackMoney voViewFinanceBackMoney = new VoViewFinanceBackMoney();
            voViewFinanceBackMoney.setMoney(StringHelper.formatMon(p.getValidMoney() / 100D));
            voViewFinanceBackMoney.setBorrowName(financePlan.getName());
            List<FinancePlanCollection> tempCollection = financePlanCollections.stream()
                    .filter(w -> w.getStatus() == 0)
                    .collect(Collectors.toList());
            voViewFinanceBackMoney.setOrder(tempCollection.size());
            //待收本金
            long principal = tempCollection.stream()
                    .mapToLong(s -> s.getPrincipal()).sum();
            //待收利息
            long interest = tempCollection.stream()
                    .mapToLong(s -> s.getInterest()).sum();
            //待收本息
            long collectionMoney = tempCollection.stream()
                    .mapToLong(w -> w.getPrincipal() + w.getInterest()).sum();

            voViewFinanceBackMoney.setCollectionMoney(StringHelper.formatMon(collectionMoney / 100D));
            voViewFinanceBackMoney.setInterest(StringHelper.formatMon(interest / 100D));
            voViewFinanceBackMoney.setPrincipal(StringHelper.formatMon(principal / 100D));
            voViewFinanceBackMoney.setBuyerId(p.getId());
            voViewFinanceBackMoney.setCreatedAt(DateHelper.dateToString(p.getCreatedAt()));
            voViewFinanceBackMoney.setPlanId(financePlan.getId());
            viewFinanceBackMoneyList.add(voViewFinanceBackMoney);
        });
        resultMaps.put("backMoneyList", viewFinanceBackMoneyList);
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
        //理财计划购买记录
        List<FinancePlanBuyer> financePlanBuyerList = (List<FinancePlanBuyer>) resultMaps.get("financePlanBuyerList");
        if (CollectionUtils.isEmpty(financePlanBuyerList)) {
            resultMaps.put("settleResList", new ArrayList<>(0));
            return resultMaps;
        }

        //理财计划id集合
        Set<Long> planIds = financePlanBuyerList.stream()
                .map(p -> p.getPlanId())
                .collect(Collectors.toSet());
        Specification<FinancePlan> fps = Specifications
                .<FinancePlan>and()
                .in("id", planIds.toArray())
                .build();
        List<FinancePlan> financePlanList = financePlanService.findList(fps);
        Map<Long, FinancePlan> financePlanMap = financePlanList.stream()
                .collect(Collectors.toMap(FinancePlan::getId, Function.identity()));

        List<VoViewFinanceBiddingRes> voViewFinanceBiddingResList = new ArrayList<>();
        financePlanBuyerList.stream().forEach(p -> {
            VoViewFinanceBiddingRes voViewFinanceBiddingRes = new VoViewFinanceBiddingRes();
            FinancePlan financePlan = financePlanMap.get(p.getPlanId());
            voViewFinanceBiddingRes.setTenderId(p.getId());
            Double aDouble = financePlan.getMoneyYes().doubleValue() / financePlan.getMoney().doubleValue();
            voViewFinanceBiddingRes.setSpend(new Double(StringHelper.formatDouble(aDouble, false)));
            voViewFinanceBiddingRes.setCreatedAt(DateHelper.dateToString(p.getCreatedAt()));
            Integer timeLimit = financePlan.getTimeLimit();
            voViewFinanceBiddingRes.setTimeLimit(timeLimit + BorrowContants.MONTH);
            Long validMoney = p.getValidMoney();
            Integer apr = p.getApr();

            //预期收益
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(new Double(validMoney), new Double(apr),
                    financePlan.getTimeLimit(), DateHelper.subDays(financePlan.getSuccessAt(), 1));
            Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(2);
            Integer earnings = NumberHelper.toInt(StringHelper.toString(calculatorMap.get("earnings")));

            voViewFinanceBiddingRes.setExpectEarnings(StringHelper.formatMon(earnings / 100D));
            voViewFinanceBiddingRes.setApr(StringHelper.formatMon(apr / 100D));
            voViewFinanceBiddingRes.setMoney(StringHelper.formatMon(validMoney / 100D));
            voViewFinanceBiddingRes.setBorrowName(financePlan.getName());
            voViewFinanceBiddingRes.setTenderId(p.getId());
            voViewFinanceBiddingRes.setPlanId(financePlan.getId());
            voViewFinanceBiddingResList.add(voViewFinanceBiddingRes);
        });
        resultMaps.put("biddingResList", voViewFinanceBiddingResList);
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
        //理财计划购买记录
        List<FinancePlanBuyer> financePlanBuyerList = (List<FinancePlanBuyer>) resultMaps.get("financePlanBuyerList");
        if (CollectionUtils.isEmpty(financePlanBuyerList)) {
            resultMaps.put("settleResList", new ArrayList<>(0));
            return resultMaps;
        }
        //理财计划id集合
        Set<Long> planIds = financePlanBuyerList.stream()
                .map(p -> p.getPlanId())
                .collect(Collectors.toSet());
        Specification<FinancePlan> fps = Specifications
                .<FinancePlan>and()
                .in("id", planIds.toArray())
                .build();
        List<FinancePlan> financePlanList = financePlanService.findList(fps);
        Map<Long, FinancePlan> financePlanMap = financePlanList.stream()
                .collect(Collectors.toMap(FinancePlan::getId, Function.identity()));
        //查询理财计划回款记录
        Set<Long> buyerIds = financePlanBuyerList.stream().map(FinancePlanBuyer::getId).collect(Collectors.toSet());
        Specification<FinancePlanCollection> fpcs = Specifications
                .<FinancePlanCollection>and()
                .in("buyerId", buyerIds.toArray())
                .build();
        List<FinancePlanCollection> financePlanCollectionList = financePlanCollectionService.findList(fpcs);
        Map<Long, List<FinancePlanCollection>> financePlanCollectionMap = financePlanCollectionList.stream()
                .collect(groupingBy(FinancePlanCollection::getBuyerId));

        List<VoViewFinanceSettleRes> voViewFinanceSettleResArrayList = new ArrayList<>();
        financePlanBuyerList.stream().forEach(p -> {
            VoViewFinanceSettleRes voViewFinanceSettleRes = new VoViewFinanceSettleRes();
            FinancePlan financePlan = financePlanMap.get(p.getPlanId());
            List<FinancePlanCollection> financePlanCollections = financePlanCollectionMap.get(p.getId());

            voViewFinanceSettleRes.setBorrowName(financePlan.getName());
            voViewFinanceSettleRes.setMoney(StringHelper.formatMon(p.getValidMoney() / 100D));
            voViewFinanceSettleRes.setCloseAt(DateHelper.dateToString(financePlan.getEndLockAt()));
            List<FinancePlanCollection> financePlanCollectionList1 = financePlanCollections.stream()
                    .filter(w -> w.getStatus() == 1)
                    .collect(Collectors.toList());
            long interest = financePlanCollectionList1.stream()
                    .mapToLong(s -> s.getInterest()).sum();
            long principal = financePlanCollectionList1.stream().
                    mapToLong(s -> s.getPrincipal()).sum();
            long collectionMoneyYes = financePlanCollectionList1.stream()
                    .mapToLong(s -> s.getPrincipal() + s.getInterest()).sum();
            voViewFinanceSettleRes.setInterest(StringHelper.formatMon(interest / 100D));
            voViewFinanceSettleRes.setPrincipal(StringHelper.formatMon(principal / 100D));
            voViewFinanceSettleRes.setCreatedAt(DateHelper.dateToString(p.getCreatedAt()));
            voViewFinanceSettleRes.setCollectionMoneyYes(StringHelper.formatMon(collectionMoneyYes / 100D));
            voViewFinanceSettleRes.setCloseAt(DateHelper.dateToString(p.getUpdatedAt(), DateHelper.DATE_FORMAT_YMD));
            voViewFinanceSettleRes.setTenderId(p.getId());
            voViewFinanceSettleRes.setPlanId(financePlan.getId());
            voViewFinanceSettleRes.setRemark("正常结清");
            voViewFinanceSettleResArrayList.add(voViewFinanceSettleRes);
        });
        resultMaps.put("settleResList", voViewFinanceSettleResArrayList);
        return resultMaps;
    }

    /**
     * 公共查询
     *
     * @param voInvestListReq
     * @return
     */
    private Map<String, Object> commonQuery(VoInvestListReq voInvestListReq) {
        Specification<FinancePlanBuyer> specification = Specifications.<FinancePlanBuyer>and()
                .eq("userId", voInvestListReq.getUserId())
                .eq("state", voInvestListReq.getType())
                .eq("status", 1)
                .build();
        List<FinancePlanBuyer> financePlanBuyerList = financePlanBuyerService.findList(specification,
                new PageRequest(voInvestListReq.getPageIndex(),
                        voInvestListReq.getPageSize(),
                        Sort.Direction.DESC, "createdAt"));
        int totalCount = financePlanBuyerList.size();
        Map<String, Object> resultMaps = Maps.newHashMap();
        resultMaps.put("totalCount", totalCount);
        resultMaps.put("financePlanBuyerList", financePlanBuyerList);

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
        //还款方式
        if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_MONTH) {
            item.setRepayFashion(BorrowContants.REPAY_FASHION_MONTH_STR);
        } else if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL) {
            item.setRepayFashion(BorrowContants.REPAY_FASHION_INTEREST_THEN_PRINCIPAL_STR);
        } else if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE) {
            item.setRepayFashion(BorrowContants.REPAY_FASHION_ONCE_STR);
        }

        item.setCreatedAt(DateHelper.dateToString(tender.getCreatedAt()));
        item.setBorrowName(borrow.getName());
        //状态
        if (tender.getState() == TenderConstans.BIDDING) {
            item.setStatusStr(TenderConstans.BIDDING_STR);
            item.setSuccessAt("");
        }
        if (tender.getState() == TenderConstans.BACK_MONEY) {
            item.setStatusStr(TenderConstans.BACK_MONEY_STR);
        }
        if (tender.getState() == TenderConstans.SETTLE) {
            item.setStatusStr(TenderConstans.SETTLE_STR);
        }

        Integer timeLimit = 0;    //期限
        Integer apr = 0;   //年华率
        Date successAt = null;  //
        Boolean falg = false;
        if (StringUtils.isEmpty(tender.getTransferBuyId())) {
            timeLimit = borrow.getTimeLimit();
            apr = borrow.getApr();
            //满标时间
            successAt = borrow.getRecheckAt();
            //期限
            if (borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_ONCE) {
                item.setRepayFashion(BorrowContants.REPAY_FASHION_ONCE_STR);
                item.setTimeLimit(timeLimit + BorrowContants.DAY);
            } else {
                item.setTimeLimit(timeLimit + BorrowContants.MONTH);
            }

            item.setSuccessAt(DateHelper.dateToString(successAt));
        } else {
            String sqlStr = "SELECT transfer.* FROM gfb_transfer transfer  " +
                    "LEFT JOIN " +
                    "gfb_transfer_buy_log  transferLog " +
                    "ON " +
                    "transfer.id=transferLog.transfer_id " +
                    "WHERE " +
                    "transferLog.id=:transferLog";
            Query query = entityManager.createNativeQuery(sqlStr, Transfer.class);
            query.setParameter("transferLog", tender.getTransferBuyId());
            List<Transfer> transfers = query.getResultList();
            if (!CollectionUtils.isEmpty(transfers)) {
                Transfer transfer = transfers.get(0);
                timeLimit = transfer.getTimeLimit();
                apr = transfer.getApr();
                //满标时间
                successAt = transfer.getRecheckAt();
                item.setTimeLimit(transfer.getTimeLimit() + BorrowContants.MONTH);

                item.setSuccessAt(transfer.getState() == TransferContants.TRANSFERED ? DateHelper.dateToString(successAt) : "");
            }
            falg = true;
        }
        if (tender.getState() != TenderConstans.BIDDING) {
            //应收利息
            BorrowCalculatorHelper borrowCalculatorHelper = new BorrowCalculatorHelper(new Double(tender.getValidMoney()), new Double(apr), timeLimit, successAt);
            Map<String, Object> calculatorMap = borrowCalculatorHelper.simpleCount(borrow.getRepayFashion());
            Integer earnings = NumberHelper.toInt(StringHelper.toString(calculatorMap.get("earnings")));
            item.setReceivableInterest(StringHelper.formatMon(earnings / 100D));
            List<BorrowCollection> borrowCollectionList = borrowCollectionRepository.findByTenderId(tender.getId());
            //利息
            Long interest = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToLong(s -> s.getInterest())
                    .sum();
            //本金
            Long principal = borrowCollectionList.stream()
                    .filter(w -> w.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToLong(s -> s.getPrincipal())
                    .sum();

            item.setInterest(StringHelper.formatMon(interest / 100D));
            item.setPrincipal(StringHelper.formatMon(principal / 100D));
        }

        //年利率
        item.setApr(StringHelper.formatMon(apr / 100D));
        item.setStatus(tender.getState());
        item.setMoney(StringHelper.formatMon(tender.getValidMoney() / 100D));
        item.setIsTransfer(falg);
        item.setBorrowId(borrow.getId());
        item.setTenderId(tender.getId());
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
        Long collectionMoneySum = borrowCollectionList.stream().mapToLong(p -> p.getCollectionMoney()).sum();
        viewReturnedMoney.setCollectionMoneySum(StringHelper.formatMon(collectionMoneySum / 100D));

        List<ReturnedMoney> returnedMonies = new ArrayList<>(0);
        borrowCollectionList.stream().forEach(p -> {
            ReturnedMoney returnedMoney = new ReturnedMoney();
            returnedMoney.setInterest(StringHelper.formatMon(p.getInterest() / 100D));
            returnedMoney.setPrincipal(StringHelper.formatMon(p.getPrincipal() / 100D));
            returnedMoney.setCollectionMoney(StringHelper.formatMon(p.getCollectionMoney() / 100D));
            returnedMoney.setOrder(p.getOrder() + 1);
            Date nowDate = DateHelper.endOfDate(new Date());
            Date collectionAt = DateHelper.endOfDate(p.getCollectionAt());
            //已还款或者 未到回款日
            returnedMoney.setLateDays(p.getStatus() == BorrowCollectionContants.STATUS_YES || collectionAt.getTime() > nowDate.getTime()
                    ? p.getLateDays()
                    : DateHelper.diffInDays(DateHelper.beginOfDate(nowDate), DateHelper.beginOfDate(collectionAt), false));
            returnedMoney.setCollectionAt(p.getStatus() == BorrowCollectionContants.STATUS_YES ?
                    DateHelper.dateToString(p.getCollectionAtYes(), DateHelper.DATE_FORMAT_YMD) :
                    DateHelper.dateToString(p.getCollectionAt(), DateHelper.DATE_FORMAT_YMD));
            returnedMoney.setStatus(p.getStatus());
            returnedMonies.add(returnedMoney);
        });
        viewReturnedMoney.setReturnedMonies(returnedMonies);
        return Optional.ofNullable(viewReturnedMoney).orElse(null);
    }

    public static void main(String[] args) {


        System.out.print(DateHelper.diffInDays(DateHelper.stringToDate("2017-09-07 00:00:00"), new Date(), false));
    }
}
