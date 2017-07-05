package com.gofobao.framework.collection.biz.impl;

import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.biz.PaymentBiz;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.collection.vo.request.OrderListReq;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionDaysWarpRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListWarpResp;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailResp;
import com.gofobao.framework.collection.vo.response.web.CollectionList;
import com.gofobao.framework.collection.vo.response.web.VoViewCollectionListWarpRes;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.StringHelper;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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
        } catch (Exception e) {
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
            List<CollectionList> borrowCollections = (List<CollectionList>) resultMaps.get("borrowCollections");
            VoViewCollectionListWarpRes warpRes = VoBaseResp.ok("", VoViewCollectionListWarpRes.class);
            warpRes.setLists(borrowCollections);
            warpRes.setTotalCount(totalCount);
            return ResponseEntity.ok(warpRes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR,
                            "非法请求",
                            VoViewCollectionListWarpRes.class));

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
        } catch (Exception e) {
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
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(VoBaseResp.error(VoBaseResp.ERROR, "查询失败", VoViewCollectionDaysWarpRes.class));

        }
    }
}
