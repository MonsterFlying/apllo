package com.gofobao.framework.collection.biz.impl;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.biz.PaymentBiz;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.*;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import groovy.util.logging.Slf4j;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<VoViewCollectionOrderListResWarpResp> orderList(VoCollectionOrderReq voCollectionOrderReq) {

        List<BorrowCollection> borrowCollections = borrowCollectionService.orderList(voCollectionOrderReq);
        if (Collections.isEmpty(borrowCollections)) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "非法请求", VoViewCollectionOrderListResWarpResp.class));
        }
        try {
            List<Long> borrowIdArray = borrowCollections.stream().filter(w-> !StringUtils.isEmpty(w.getBorrowId()))
                    .map(p -> p.getBorrowId())
                    .collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());
            List<Borrow> borrowList = borrowRepository.findByIdIn(borrowIdArray);
            Map<Long, Borrow> borrowMap = borrowList
                    .stream()
                    .collect(Collectors.toMap(Borrow::getId, Function.identity()));

            VoViewCollectionOrderListWarpResp voViewCollectionOrderListWarpRespRes = new VoViewCollectionOrderListWarpResp();
            List<VoViewCollectionOrderRes> orderResList = new ArrayList<>();
            borrowCollections.stream().forEach(p -> {
                VoViewCollectionOrderRes item = new VoViewCollectionOrderRes();
                Borrow borrow = borrowMap.get(p.getBorrowId());
                item.setBorrowName(borrow.getName());
                item.setOrder(p.getOrder() + 1);
                item.setTimeLime(borrow.getTimeLimit());
                item.setCollectionMoney(StringHelper.formatMon(p.getCollectionMoney() / 100d));
                item.setCollectionMoneyYes(StringHelper.formatMon(p.getCollectionMoneyYes() / 100d));
                orderResList.add(item);
            });
            //回款列表
            voViewCollectionOrderListWarpRespRes.setOrderResList(orderResList);
            //总回款期数
            voViewCollectionOrderListWarpRespRes.setOrder(orderResList.size());
            //已回款金额
            Integer sumCollectionMoneyYes = borrowCollections.stream()
                    .filter(p -> p.getStatus() == BorrowCollectionContants.STATUS_YES)
                    .mapToInt(w -> w.getCollectionMoneyYes())
                    .sum();
            voViewCollectionOrderListWarpRespRes.setSumCollectionMoneyYes(StringHelper.formatMon(sumCollectionMoneyYes / 100d));

            VoViewCollectionOrderListResWarpResp warpRes = VoBaseResp.ok("查询成功", VoViewCollectionOrderListResWarpResp.class);
            List<VoViewCollectionOrderListWarpResp> orderLists = new ArrayList<>(0);
            orderLists.add(voViewCollectionOrderListWarpRespRes);
            warpRes.setListRes(orderLists);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "非法请求", VoViewCollectionOrderListResWarpResp.class));
        }

    }

    /**
     * 回款详情
     *
     * @param voOrderDetailReq
     * @return
     */
    @Override
    public ResponseEntity<VoViewOrderDetailWarpRes> orderDetail(VoOrderDetailReq voOrderDetailReq) {
        try {
            VoViewOrderDetailResp detailRes = borrowCollectionService.orderDetail(voOrderDetailReq);
            VoViewOrderDetailWarpRes resWarpRes = VoBaseResp.ok("查询成功", VoViewOrderDetailWarpRes.class);
            resWarpRes.setDetailWarpRes(detailRes);
            return ResponseEntity.ok(resWarpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewOrderDetailWarpRes.class));
        }
    }

    @Override
    public ResponseEntity<VoViewCollectionDaysWarpRes> collectionDays(String date, Long userId) {
        VoViewCollectionDaysWarpRes collectionDayWarpRes = VoBaseResp.ok("查询成功", VoViewCollectionDaysWarpRes.class);
        try {
            Date date1 = DateHelper.stringToDate(date, "yyyy-MM");
            List<Integer> result = borrowCollectionService.collectionDay(date, userId);
            collectionDayWarpRes.setWarpRes(result);
            return ResponseEntity.ok(collectionDayWarpRes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewCollectionDaysWarpRes.class));

        }
    }
}
