package com.gofobao.framework.finance.biz.impl;

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
import com.gofobao.framework.finance.biz.FinanceCollectionBiz;
import com.gofobao.framework.finance.service.FinancePlanService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.system.biz.ThirdBatchDealLogBiz;
import com.gofobao.framework.tender.contants.BorrowContants;
import com.google.common.base.Preconditions;
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
public class FinanceCollectionBizImpl implements FinanceCollectionBiz {

    @Autowired
    private BorrowCollectionService borrowCollectionService;
    @Autowired
    private BorrowRepository borrowRepository;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private BorrowRepaymentService borrowRepaymentService;

    @Autowired
    private ThirdBatchDealLogBiz thirdBatchDealLogBiz;
    @Autowired
    private FinancePlanService financePlanService;


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
            long sumCollectionMoneyYes = 0;
            for (BorrowCollection borrowCollection : borrowCollections) {
                VoViewCollectionOrderRes item = new VoViewCollectionOrderRes();
                Borrow borrow = borrowMap.get(borrowCollection.getBorrowId());
                /*回款对应期数还款记录*/
                Specification<BorrowRepayment> brs = Specifications
                        .<BorrowRepayment>and()
                        .eq("borrowId", borrow.getId())
                        .eq("order", borrowCollection.getOrder())
                        .build();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(brs);
                Preconditions.checkState(!CollectionUtils.isEmpty(borrowRepaymentList), "还款记录不存在!");
                item.setBorrowName(borrow.getName());
                item.setStatus(borrowCollection.getStatus());
                item.setCollectionId(borrowCollection.getId());
                item.setOrder(borrowCollection.getOrder() + 1);
                item.setTimeLime(borrow.getRepayFashion() == BorrowContants.REPAY_FASHION_YCBX_NUM ? 1 : borrow.getTimeLimit());
                item.setCollectionMoney(StringHelper.formatMon(borrowCollection.getCollectionMoney() / 100d));
                if (borrowCollection.getStatus().intValue() == BorrowCollectionContants.STATUS_YES.intValue()) {  // 已还款
                    item.setCollectionMoneyYes(StringHelper.formatMon(borrowCollection.getCollectionMoneyYes() / 100d));
                    sumCollectionMoneyYes += borrowCollection.getCollectionMoney();
                } else {
                    item.setCollectionMoneyYes(StringHelper.formatMon(0));
                }

                orderResList.add(item);
            }

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


}
