package com.gofobao.framework.collection.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.biz.PaymentBiz;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.collection.vo.request.OrderListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionDaysWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListWarpResp;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailResp;
import com.gofobao.framework.collection.vo.response.web.Collection;
import com.gofobao.framework.collection.vo.response.web.*;
import com.gofobao.framework.common.jxl.ExcelException;
import com.gofobao.framework.common.jxl.ExcelUtil;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.StringHelper;
import com.google.common.collect.Maps;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/6/6.
 */


@Service
@Slf4j
public class PaymentBizImpl implements PaymentBiz {

    @Autowired
    private BorrowCollectionService borrowCollectionService;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BorrowService borrowService;

    /**
     * 回款列表
     *
     * @param voCollectionOrderReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewCollectionOrderListWarpResp> orderList(VoCollectionOrderReq voCollectionOrderReq) {
        VoViewCollectionOrderListWarpResp warpResp = new VoViewCollectionOrderListWarpResp();
        List<BorrowCollection> borrowCollections = borrowCollectionService.orderList(voCollectionOrderReq);
        if (Collections.isEmpty(borrowCollections)) {
            VoViewCollectionOrderListWarpResp error = VoBaseResp.ok("查询成功", VoViewCollectionOrderListWarpResp.class);
            error.setOrder(0);
            error.setSumCollectionMoneyYes("0");
            error.setOrderResList(new ArrayList<>());
            return ResponseEntity.ok(error);
        }
        try {
            List<Long> borrowIdArray = borrowCollections.stream().filter(w -> !StringUtils.isEmpty(w.getBorrowId()))
                    .map(p -> p.getBorrowId())
                    .collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());
            List<Borrow> borrowList = borrowRepository.findByIdIn(borrowIdArray);
            Map<Long, Borrow> borrowMap = borrowList
                    .stream()
                    .collect(Collectors.toMap(Borrow::getId, Function.identity()));

            List<VoViewCollectionOrderRes> orderResList = new ArrayList<>();
            borrowCollections.stream().forEach(p -> {
                VoViewCollectionOrderRes item = new VoViewCollectionOrderRes();
                Borrow borrow = borrowMap.get(p.getBorrowId());
                item.setBorrowName(borrow.getName());
                item.setCollectionId(p.getId());
                item.setOrder(p.getOrder() + 1);
                item.setTimeLime(borrow.getTimeLimit());
                item.setCollectionMoney(StringHelper.formatMon(p.getCollectionMoney() / 100d));
                item.setCollectionMoneyYes(StringHelper.formatMon(p.getCollectionMoneyYes() / 100d));
                orderResList.add(item);
            });

            Integer sumCollectionMoneyYes = borrowCollections.stream()
                    .filter(p -> p.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToInt(w -> w.getCollectionMoneyYes())
                    .sum();
            warpResp.setSumCollectionMoneyYes(StringHelper.formatMon(sumCollectionMoneyYes / 100d));

            VoViewCollectionOrderListWarpResp warpRes = VoBaseResp.ok("查询成功", VoViewCollectionOrderListWarpResp.class);
            //总回款期数
            warpRes.setOrder(orderResList.size());
            //已回款金额
            warpRes.setSumCollectionMoneyYes(StringHelper.formatMon(sumCollectionMoneyYes / 100d));
            //回款列表
            warpRes.setOrderResList(orderResList);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "非法请求", VoViewCollectionOrderListWarpResp.class));
        }

    }

    /**
     * PC：回款明细
     *
     * @param orderListReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewCollectionListWarpRes> pcOrderList(OrderListReq orderListReq) {
        try {
            Map<String, Object> resultMaps = borrowCollectionService.pcOrderList(orderListReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<CollectionList> borrowCollections = (List<CollectionList>) resultMaps.get("orderList");
            VoViewCollectionListWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewCollectionListWarpRes.class);
            warpRes.setLists(borrowCollections);
            warpRes.setTotalCount(totalCount);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "非法请求",
                            VoViewCollectionListWarpRes.class));

        }
    }


    @Override
    public void toExcel(HttpServletResponse response, OrderListReq listReq) {

        List<CollectionList> collectionLists = borrowCollectionService.toExecl(listReq);
        if(!CollectionUtils.isEmpty(collectionLists)){
            LinkedHashMap<String,String >paramMaps=Maps.newLinkedHashMap();
            paramMaps.put("createTime","时间");
            paramMaps.put("collectionMoney","待收本息");
            paramMaps.put("principal","待收本金");
            paramMaps.put("interest","待收利息");
            paramMaps.put("orderCount","笔数");
            try {
                ExcelUtil.listToExcel(collectionLists,paramMaps,"回款明细",response);
            } catch (ExcelException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * pc: 回款详情
     *
     * @param listReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewCollectionWarpRes> pcOrderDetail(VoCollectionListReq listReq) {
        try {
            Map<String, Object> resultMaps = borrowCollectionService.pcCollectionsByDay(listReq);
            Integer totalCount = Integer.valueOf(resultMaps.get("totalCount").toString());
            List<Collection> borrowCollections = (List<Collection>) resultMaps.get("collectionList");
            VoViewCollectionWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewCollectionWarpRes.class);
            warpRes.setTotalCount(totalCount);
            warpRes.setCollections(borrowCollections);
            return ResponseEntity.ok(warpRes);
        } catch (Throwable e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "非法请求",
                            VoViewCollectionWarpRes.class));

        }
    }

    /**
     * 回款详情
     *
     * @param voOrderDetailReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewOrderDetailResp> orderDetail(VoOrderDetailReq voOrderDetailReq) {
        try {
            VoViewOrderDetailResp detailRes = borrowCollectionService.orderDetail(voOrderDetailReq);
            return ResponseEntity.ok(detailRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewOrderDetailResp.class));
        }
    }

    @Override
    public ResponseEntity<VoViewCollectionDaysWarpRes> collectionDays(String date, Long userId) {
        VoViewCollectionDaysWarpRes collectionDayWarpRes = VoBaseResp.ok("查询成功", VoViewCollectionDaysWarpRes.class);
        try {
            List<Integer> result = borrowCollectionService.collectionDay(date, userId);
            collectionDayWarpRes.setWarpRes(result);
            return ResponseEntity.ok(collectionDayWarpRes);
        } catch (Throwable e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewCollectionDaysWarpRes.class));

        }
    }


    /**
     * 根据时间查询回款列表
     *
     * @param dateStr
     * @param userId
     * @return
     */
    public ResponseEntity<VoCollectionListByDays> collectionListByDays(String dateStr, Long userId) {

        Date date = DateHelper.stringToDate(dateStr, DateHelper.DATE_FORMAT_YMD);

        VoCollectionListByDays voCollectionListByDays = VoCollectionListByDays.ok("查询成功!", VoCollectionListByDays.class);
        List<CollectionDetail> collectionDetailList = new ArrayList<>();

        Specification<BorrowCollection> bcs = Specifications
                .<BorrowCollection>and()
                .eq("userId", userId)
                .between("collectionAt", new Range<>(DateHelper.beginOfDate(date), DateHelper.endOfDate(date)))
                .eq("status", 0)
                .build();
        List<BorrowCollection> borrowCollectionList = borrowCollectionService.findList(bcs);
        List<Long> borrowIds = new ArrayList<>();
        if (CollectionUtils.isEmpty(borrowCollectionList)) {
            return ResponseEntity.ok(voCollectionListByDays);
        }

        for (BorrowCollection borrowCollection : borrowCollectionList) {
            borrowIds.add(borrowCollection.getBorrowId());
        }

        Specification<Borrow> bs = Specifications
                .<Borrow>and()
                .in("id", borrowIds.toArray())
                .build();
        List<Borrow> borrowList = borrowService.findList(bs);

        CollectionDetail collectionDetail = null;
        for (BorrowCollection borrowCollection : borrowCollectionList) {
            for (Borrow borrow : borrowList) {
                if (String.valueOf(borrowCollection.getBorrowId()).equals(String.valueOf(borrow.getId()))) {
                    collectionDetail = new CollectionDetail();
                    collectionDetail.setName(borrow.getName());
                    collectionDetail.setCollectionAt(DateHelper.dateToString(borrowCollection.getCollectionAt(), DateHelper.DATE_FORMAT_YMD));
                    collectionDetail.setPrincipal(StringHelper.formatDouble(borrowCollection.getPrincipal(), 100, false));
                    int money = borrowCollection.getInterest();
                    if (borrow.getType() == 0 || borrow.getType() == 4) {
                        money -= (int) MathHelper.myRound(borrowCollection.getInterest() * 0.9, 0);
                    }
                    collectionDetail.setEarnings(StringHelper.formatDouble(money, 100, false));
                    collectionDetail.setInterest(StringHelper.formatDouble(borrowCollection.getInterest(), 100, false));
                    collectionDetail.setOrderStr(borrowCollection.getOrder() + 1 + "/" + borrow.getTotalOrder());
                    collectionDetailList.add(collectionDetail);
                }
            }
        }
        voCollectionListByDays.setCollectionDetailList(collectionDetailList);
        return ResponseEntity.ok(voCollectionListByDays);
    }
}
