package com.gofobao.framework.finance.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionDaysWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListWarpResp;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailResp;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.biz.FinanceCollectionBiz;
import com.gofobao.framework.finance.entity.FinancePlan;
import com.gofobao.framework.finance.entity.FinancePlanBuyer;
import com.gofobao.framework.finance.entity.FinancePlanCollection;
import com.gofobao.framework.finance.service.FinancePlanBuyerService;
import com.gofobao.framework.finance.service.FinancePlanCollectionService;
import com.gofobao.framework.finance.service.FinancePlanService;
import com.gofobao.framework.finance.vo.request.VoFinanceCollectionDetailReq;
import com.gofobao.framework.finance.vo.response.VoViewFinanceCollectionDetailResp;
import com.gofobao.framework.finance.vo.response.VoViewFinanceCollectionListResp;
import com.gofobao.framework.finance.vo.response.VoViewFinanceCollectionRes;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.system.biz.ThirdBatchDealLogBiz;
import com.gofobao.framework.system.vo.response.VoViewFindLendRepayStatusListRes;
import com.gofobao.framework.tender.contants.BorrowContants;
import com.google.common.base.Preconditions;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/6/6.
 */


@Service
@Slf4j
public class FinanceCollectionBizImpl implements FinanceCollectionBiz {

    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private FinancePlanService financePlanService;
    @Autowired
    private FinancePlanBuyerService financePlanBuyerService;
    @Autowired
    private FinancePlanCollectionService financePlanCollectionService;


    /**
     * 回款列表
     *
     * @param voCollectionOrderReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewFinanceCollectionListResp> orderList(VoCollectionOrderReq voCollectionOrderReq) {
        VoViewFinanceCollectionListResp voViewFinanceCollectionListResp = VoBaseResp.ok("查询成功!", VoViewFinanceCollectionListResp.class);
        voViewFinanceCollectionListResp.setSumCollectionMoneyYes(StringHelper.formatDouble(0, 100d, true));
        voViewFinanceCollectionListResp.setOrder(0);
        voViewFinanceCollectionListResp.setOrderResList(new ArrayList<>());
        //需要查询的用户id
        long userId = voCollectionOrderReq.getUserId();
        Date date = DateHelper.stringToDate(voCollectionOrderReq.getTime(), DateHelper.DATE_FORMAT_YMD);/*yyyy-MM-dd*/
        //查询理财计划购买记录
        Specification<FinancePlanBuyer> fpbs = Specifications
                .<FinancePlanBuyer>and()
                .eq("userId", userId)
                .eq("status", 1)
                .build();
        List<FinancePlanBuyer> financePlanBuyerList = financePlanBuyerService.findList(fpbs);
        if (CollectionUtils.isEmpty(financePlanBuyerList)) {
            return ResponseEntity.ok(voViewFinanceCollectionListResp);
        }
        //理财计划购买id集合
        Set<Long> buyerIds = financePlanBuyerList.stream().map(FinancePlanBuyer::getId).collect(Collectors.toSet());
        /*理财计划id集合*/
        Set<Long> planIds = financePlanBuyerList.stream().map(FinancePlanBuyer::getPlanId).collect(Collectors.toSet());
        Specification<FinancePlan> fps = Specifications
                .<FinancePlan>and()
                .in("id", planIds.toArray())
                .eq("status", 3)
                .build();
        List<FinancePlan> financePlanList = financePlanService.findList(fps);
        if (CollectionUtils.isEmpty(financePlanList)) {
            return ResponseEntity.ok(voViewFinanceCollectionListResp);
        }
        Map<Long/*planId*/, FinancePlan> financePlanMap = financePlanList.stream().collect(Collectors.toMap(FinancePlan::getId, Function.identity()));
        /*理财计划id集合*/
        planIds = financePlanList.stream().map(FinancePlan::getId).collect(Collectors.toSet());
        //查询理财计划未回款记录
        Specification<FinancePlanCollection> fpcs = Specifications
                .<FinancePlanCollection>and()
                .between("collectionAt", new Range(DateHelper.beginOfDate(date), DateHelper.endOfDate(date)))
                .eq("userId", userId)
                .in("planId", planIds.toArray())
                .in("buyerId", buyerIds.toArray())
                .build();
        List<FinancePlanCollection> financePlanCollectionList = financePlanCollectionService.findList(fpcs, new PageRequest(voCollectionOrderReq.getPageIndex(), voCollectionOrderReq.getPageSize()));
        List<VoViewFinanceCollectionRes> financeCollectionResList = new ArrayList<>();
        long sumCollectionMoneyYes = 0;
        for (FinancePlanCollection financePlanCollection : financePlanCollectionList) {
            long principal = financePlanCollection.getPrincipal();
            long interest = financePlanCollection.getInterest();
            long collectionYes = financePlanCollection.getStatus() == 1 ? principal + interest : 0;
            FinancePlan financePlan = financePlanMap.get(financePlanCollection.getPlanId());
            sumCollectionMoneyYes += collectionYes;

            VoViewFinanceCollectionRes voViewFinanceCollectionRes = new VoViewFinanceCollectionRes();
            voViewFinanceCollectionRes.setCollectionId(financePlanCollection.getId());
            voViewFinanceCollectionRes.setCollectionMoney(StringHelper.formatDouble(principal + interest, 100d, true));
            voViewFinanceCollectionRes.setCollectionMoneyYes(StringHelper.formatDouble(collectionYes, 100d, true));
            voViewFinanceCollectionRes.setFinanceName(financePlan.getName());
            voViewFinanceCollectionRes.setOrder(financePlanCollection.getOrderNum() + 1);
            voViewFinanceCollectionRes.setStatus(financePlanCollection.getStatus());
            voViewFinanceCollectionRes.setTimeLime(financePlan.getTimeLimit());

            financeCollectionResList.add(voViewFinanceCollectionRes);
        }
        voViewFinanceCollectionListResp.setSumCollectionMoneyYes(StringHelper.formatDouble(sumCollectionMoneyYes, 100d, true));
        voViewFinanceCollectionListResp.setOrder(financePlanCollectionList.size());
        voViewFinanceCollectionListResp.setOrderResList(financeCollectionResList);
        return ResponseEntity.ok(voViewFinanceCollectionListResp);
    }

    /**
     * 回款详情
     *
     * @param voFinanceCollectionDetailReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewFinanceCollectionDetailResp> orderDetail(VoFinanceCollectionDetailReq voFinanceCollectionDetailReq) {
        VoViewFinanceCollectionDetailResp detailRes = VoBaseResp.ok("查询成功", VoViewFinanceCollectionDetailResp.class);
        /*理财计划回款记录*/
        FinancePlanCollection financePlanCollection = financePlanCollectionService.findById(voFinanceCollectionDetailReq.getCollectionId());
        Preconditions.checkNotNull(financePlanCollection, "理财计划回款记录不存在!");
        /*理财计划记录*/
        FinancePlan financePlan = financePlanService.findById(financePlanCollection.getPlanId());
        Preconditions.checkNotNull(financePlan, "理财计划记录不存在!");

        long principal = financePlanCollection.getPrincipal();
        long interest = financePlanCollection.getInterest();
        detailRes.setOrder(financePlanCollection.getOrderNum() + 1);
        detailRes.setCollectionMoney(StringHelper.formatMon(principal + interest / 100D));
        detailRes.setBorrowName(financePlan.getName());
        if (financePlanCollection.getStatus() == 1) {
            detailRes.setStatusStr("已结息");
        } else {
            detailRes.setStatusStr("未结息");
        }
        detailRes.setCollectionAt(DateHelper.dateToString(financePlanCollection.getCollectionAt(), DateHelper.DATE_FORMAT_YMD));
        detailRes.setPrincipal(StringHelper.formatMon(principal / 100D));
        detailRes.setInterest(StringHelper.formatMon(interest / 100D));
        return ResponseEntity.ok(detailRes);
    }

    @Override
    public ResponseEntity<VoViewCollectionDaysWarpRes> collectionDays(String date, Long userId) {
        VoViewCollectionDaysWarpRes collectionDayWarpRes = VoBaseResp.ok("查询成功", VoViewCollectionDaysWarpRes.class);
        try {
            //理财计划月回款记录标识
            List<Integer> result = financePlanCollectionService.collectionDay(date, userId);
            collectionDayWarpRes.setWarpRes(result);
            return ResponseEntity.ok(collectionDayWarpRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewCollectionDaysWarpRes.class));
        }
    }


}
