package com.gofobao.framework.collection.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.contants.BorrowCollectionContants;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.repository.BorrowCollectionRepository;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderList;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailRes;
import com.gofobao.framework.helper.BeanHelper;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/5/31.
 */
@Component
public class BorrowCollectionServiceImpl implements BorrowCollectionService {

    @Autowired
    private BorrowCollectionRepository borrowCollectionRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    /**
     * 回款列表
     *
     * @param voCollectionOrderReq
     * @return VoViewCollectionOrderListRes
     */
    @Override
    public VoViewCollectionOrderList orderList(VoCollectionOrderReq voCollectionOrderReq) {
        Date date = DateHelper.stringToDate(voCollectionOrderReq.getTime());

        Specification<BorrowCollection> specification = Specifications.<BorrowCollection>and()
                .eq("userId", voCollectionOrderReq.getUserId())
                .between("startAt", new Range<>(date, DateHelper.addDays(date, 1)))
                .eq("transferFlag", BorrowCollectionContants.TRANSFER_FLAG_NO)
                .build();
        List<BorrowCollection> borrowCollections = borrowCollectionRepository.findAll(specification);
        if (CollectionUtils.isEmpty(borrowCollections)) {
            return null;
        }
        Set<Integer> borrowIdSet = borrowCollections.stream()
                .map(f -> f.getBorrowId())
                .collect(Collectors.toSet());
        List<Borrow> borrowList = borrowRepository.findByIdIn(Lists.newArrayList(borrowIdSet));
        Map<Long, Borrow> borrowMap = borrowList
                .stream()
                .collect(Collectors.toMap(Borrow::getId, Function.identity()));

        VoViewCollectionOrderList voViewCollectionOrderListRes = new VoViewCollectionOrderList();

        List<VoViewCollectionOrderRes> orderResList = new ArrayList<>();

        borrowCollections.stream().forEach(p -> {
            VoViewCollectionOrderRes item = new VoViewCollectionOrderRes();
            Borrow borrow = borrowMap.get(p.getBorrowId());
            item.setBorrowName(borrow.getName());
            item.setOrder(p.getOrder() + 1);
            item.setTimeLime(borrow.getTimeLimit());
            item.setCollectionMoney(NumberHelper.to2DigitString(p.getCollectionMoney() / 100D));
            item.setCollectionMoneyYes(NumberHelper.to2DigitString(p.getCollectionMoneyYes() / 100D));
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
        Optional<VoViewCollectionOrderList> orderListRes = Optional.ofNullable(voViewCollectionOrderListRes);
        return orderListRes.orElseGet(() -> new VoViewCollectionOrderList());
    }

    /**
     * 回款详情
     *
     * @param voOrderDetailReq
     * @return VoViewOrderDetailRes
     */
    @Override
    public VoViewOrderDetailRes orderDetail(VoOrderDetailReq voOrderDetailReq) {
        BorrowCollection borrowCollection = borrowCollectionRepository.findOne(voOrderDetailReq.getCollectionId());
        if (Objects.isNull(borrowCollection)) {
            return null;
        }
        Borrow borrow = borrowRepository.findOne(borrowCollection.getBorrowId().longValue());
        VoViewOrderDetailRes detailRes = new VoViewOrderDetailRes();
        detailRes.setOrder(borrowCollection.getOrder() + 1);
        detailRes.setCollectionMoney(NumberHelper.to2DigitString(borrowCollection.getCollectionMoney() / 100D));
        detailRes.setLateDays(borrowCollection.getLateDays());
        detailRes.setStartAt(DateHelper.dateToString(borrowCollection.getStartAtYes()));
        detailRes.setBorrowName(borrow.getName());
        Integer interest = 0;  //利息
        Integer principal = 0;//本金
        if (borrowCollection.getStatus() == 1) {
            interest = borrowCollection.getInterest();
            principal = borrowCollection.getPrincipal();
            detailRes.setStatus(BorrowCollectionContants.STATUS_YES_STR);
        } else {
            detailRes.setStatus(BorrowCollectionContants.STATUS_YES_STR);
        }
        detailRes.setPrincipal(NumberHelper.to2DigitString(interest / 100D));
        detailRes.setInterest(NumberHelper.to2DigitString(principal / 100D));
        return detailRes;
    }

    public List<BorrowCollection> findList(Specification<BorrowCollection> specification, Pageable pageable) {
        Page<BorrowCollection> page = borrowCollectionRepository.findAll(specification, pageable);
        return page.getContent();
    }

    public List<BorrowCollection> findList(Specification<BorrowCollection> specification, Sort sort) {
        return borrowCollectionRepository.findAll(specification, sort);
    }

    public boolean updateBySpecification(BorrowCollection borrowCollection, Specification<BorrowCollection> specification) {
        List<BorrowCollection> borrowCollectionList = borrowCollectionRepository.findAll(specification);
        Optional<List<BorrowCollection>> optional = Optional.ofNullable(borrowCollectionList);
        optional.ifPresent(list -> list.forEach(obj -> {
            BeanHelper.copyParamter(borrowCollection, obj, true);
        }));
        return !CollectionUtils.isEmpty(borrowCollectionRepository.save(borrowCollectionList));
    }

    public BorrowCollection save(BorrowCollection borrowCollection) {
        return borrowCollectionRepository.save(borrowCollection);
    }

    public BorrowCollection insert(BorrowCollection borrowCollection) {
        if (ObjectUtils.isEmpty(borrowCollection)) {
            return null;
        }
        borrowCollection.setId(null);
        return borrowCollectionRepository.save(borrowCollection);
    }

    public BorrowCollection updateById(BorrowCollection borrowCollection) {
        if (ObjectUtils.isEmpty(borrowCollection) || ObjectUtils.isEmpty(borrowCollection.getId())) {
            return null;
        }
        return borrowCollectionRepository.save(borrowCollection);
    }

    public List<BorrowCollection> findList(Specification<BorrowCollection> specification){
        return borrowCollectionRepository.findAll(specification);
    }

    public long count(Specification<BorrowCollection> specification){
        return borrowCollectionRepository.count(specification);
    }
}
