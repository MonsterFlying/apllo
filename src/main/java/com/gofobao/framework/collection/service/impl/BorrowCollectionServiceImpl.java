package com.gofobao.framework.collection.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.borrow.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.borrow.vo.response.VoViewCollectionOrderListRes;
import com.gofobao.framework.borrow.vo.response.VoViewCollectionOrderRes;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.google.common.base.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/5/31.
 */
@Service
public class BorrowCollectionServiceImpl implements BorrowCollectionService {

    @Autowired
    private BorrowCollectionRepository borrowCollectionRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Override
    public VoViewCollectionOrderListRes orderList(VoCollectionOrderReq voCollectionOrderReq) {
        Date date = DateHelper.stringToDate(voCollectionOrderReq.getTime());
        Specification<BorrowCollection> specification = Specifications.<BorrowCollection>and()
                .eq("userId", voCollectionOrderReq.getUserId())
                .between("startAt", new Range<>(date, DateHelper.addDays(date, 1)))
                .eq("transferFlag", BorrowCollectionContants.TRANSFER_FLAG_NO)
                .build();
        List<BorrowCollection> borrowCollections = borrowCollectionRepository.findAll(specification);
        List<Integer> borrowId = borrowCollections.stream()
                .map(f -> f.getBorrowId())
                .collect(Collectors.toList());
        List<Borrow> borrowList = borrowRepository.findByIdIn(borrowId);
        Map<Long, Borrow> borrowMap = borrowList
                .stream()
                .collect(Collectors.toMap(Borrow::getId, Function.identity()));

        VoViewCollectionOrderListRes voViewCollectionOrderListRes = new VoViewCollectionOrderListRes();

        List<VoViewCollectionOrderRes> orderResList = new ArrayList<>();

        borrowCollections.stream().forEach(p -> {
            VoViewCollectionOrderRes item = new VoViewCollectionOrderRes();
            Borrow borrow = borrowMap.get(p.getBorrowId());
            item.setBorrowName(borrow.getName());
            item.setOrder(p.getOrder() + 1);
            item.setTimeLime(borrow.getTimeLimit());
            item.setCollectionMoney(NumberHelper.to2DigitString(p.getCollectionMoney() / 100));
            item.setCollectionMoneyYes(NumberHelper.to2DigitString(p.getCollectionMoneyYes() / 100));
            orderResList.add(item);
        });
        //回款列表
        voViewCollectionOrderListRes.setOrderResList(orderResList);
        //总回款期数
        voViewCollectionOrderListRes.setOrder(orderResList.size());
        //已回款金额
        Integer sumCollectionMoneyYes = borrowCollections.stream()
                .filter(p -> p.getStatus() == BorrowCollectionContants.STATUS_YES)
                .mapToInt(w -> w.getCollectionMoneyYes())
                .sum();
        voViewCollectionOrderListRes.setSumCollectionMoneyYes(NumberHelper.to2DigitString(sumCollectionMoneyYes / 100));
        Optional<VoViewCollectionOrderListRes> orderListRes = Optional.ofNullable(voViewCollectionOrderListRes);
        return orderListRes.orElse(null);
    }
}
