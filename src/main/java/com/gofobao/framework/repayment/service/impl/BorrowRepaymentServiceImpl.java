package com.gofobao.framework.repayment.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.request.VoOrderDetailReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderListRes;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailRes;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.repayment.contants.RepaymentContants;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.repository.BorrowRepaymentRepository;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoInfoReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/6/1.
 */
@Service
public class BorrowRepaymentServiceImpl implements BorrowRepaymentService {

    @Autowired
    private BorrowRepaymentRepository borrowRepaymentRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    /**
     * 还款计划列表
     *
     * @param voCollectionOrderReq
     * @return VoViewCollectionOrderListRes
     */
    @Override
    public VoViewCollectionOrderListRes repaymentList(VoCollectionOrderReq voCollectionOrderReq) {
        Date date = DateHelper.beginOfDate(DateHelper.stringToDate(voCollectionOrderReq.getTime()));
        Specification<BorrowRepayment> specification = Specifications.<BorrowRepayment>and()
                .eq("userId", voCollectionOrderReq.getUserId())
                .between("repayAt", new Range<>(date, DateHelper.addDays(date, 1)))
                .build();
        List<BorrowRepayment> repaymentList = borrowRepaymentRepository.findAll(specification);
        if (CollectionUtils.isEmpty(repaymentList)) {
            return null;
        }
        Set<Long> borrowIdSet = repaymentList.stream()
                .map(p -> p.getBorrowId())
                .collect(Collectors.toSet());

        List<Borrow> borrowList = borrowRepository.findByIdIn(new ArrayList(borrowIdSet));
        Map<Long, Borrow> borrowMap = borrowList.stream()
                .collect(Collectors
                        .toMap(Borrow::getId, Function.identity()));
        VoViewCollectionOrderListRes orderListRes = new VoViewCollectionOrderListRes();
        List<VoViewCollectionOrderRes> orderResList = new ArrayList<>();

        repaymentList.stream().forEach(p -> {
            VoViewCollectionOrderRes collectionOrderRes = new VoViewCollectionOrderRes();
            Borrow borrow = borrowMap.get(p.getId());
            collectionOrderRes.setBorrowName(borrow.getName());
            collectionOrderRes.setOrder(p.getOrder() + 1);
            collectionOrderRes.setCollectionMoneyYes(NumberHelper.to2DigitString(p.getRepayMoneyYes() / 100));
            collectionOrderRes.setCollectionMoney(NumberHelper.to2DigitString(p.getRepayMoney() / 100));
            collectionOrderRes.setTimeLime(borrow.getTimeLimit());
            orderResList.add(collectionOrderRes);
        });

        if (CollectionUtils.isEmpty(orderResList)) {
            return null;
        }
        orderListRes.setOrderResList(orderResList);
        orderListRes.setOrder(orderResList.size());
        Integer moneyYesSum = repaymentList.stream()
                .filter(p -> p.getStatus() == 1)
                .mapToInt(w -> w.getRepayMoneyYes())
                .sum();
        orderListRes.setSumCollectionMoneyYes(NumberHelper.to2DigitString(moneyYesSum / 100));
        return orderListRes;
    }



    /**
     * 还款详情
     *
     * @param voInfoReq
     * @return VoViewOrderDetailRes
     */
    @Override
    public VoViewOrderDetailRes info(VoInfoReq voInfoReq) {
        VoViewOrderDetailRes detailRes = new VoViewOrderDetailRes();
        Specification<BorrowRepayment> specification = Specifications.<BorrowRepayment>and()
                .eq("userId", voInfoReq.getUserId())
                .eq("id", voInfoReq.getRepaymentId())
                .build();
        BorrowRepayment borrowRepayment = borrowRepaymentRepository.findOne(specification);
        if (ObjectUtils.isEmpty(borrowRepayment)) {
            return detailRes;
        }

        Long borrowId = borrowRepayment.getBorrowId();
        Borrow borrow = borrowRepository.findOne(borrowId);
        Integer principal = 0;
        Integer interest = 0;
        if (borrowRepayment.getStatus() == 0) {
            interest = borrowRepayment.getInterest();
            principal = borrowRepayment.getPrincipal();
            detailRes.setStatus(RepaymentContants.STATUS_NO_STR);
        } else {
            detailRes.setStatus(RepaymentContants.STATUS_YES_STR);
        }
        detailRes.setInterest(NumberHelper.to2DigitString(interest / 100));
        detailRes.setPrincipal(NumberHelper.to2DigitString(principal / 100));
        detailRes.setBorrowName(borrow.getName());
        detailRes.setCollectionMoney(NumberHelper.to2DigitString(borrowRepayment.getRepayMoneyYes()));
        detailRes.setLateDays(borrowRepayment.getLateDays());
        detailRes.setOrder(borrowRepayment.getOrder() + 1);
        detailRes.setStartAt(DateHelper.dateToString(borrowRepayment.getRepayAtYes()));
        return detailRes;
    }

    public BorrowRepayment save(BorrowRepayment borrowRepayment){
        return save(borrowRepayment);
    }

    public BorrowRepayment insert(BorrowRepayment borrowRepayment){
        if (ObjectUtils.isEmpty(borrowRepayment)){
            return null;
        }
        borrowRepayment.setId(null);
        return borrowRepaymentRepository.save(borrowRepayment);
    }

    public BorrowRepayment updateById(BorrowRepayment borrowRepayment){
        if (ObjectUtils.isEmpty(borrowRepayment) || ObjectUtils.isEmpty(borrowRepayment.getId())){
            return null;
        }
        return borrowRepaymentRepository.save(borrowRepayment);
    }


    public BorrowRepayment findByIdLock(Long id){
        return borrowRepaymentRepository.findById(id);
    }

    public BorrowRepayment findById(Long id){
        return borrowRepaymentRepository.findOne(id);
    }

    public List<BorrowRepayment> findList(Specification<BorrowRepayment> specification){
        return borrowRepaymentRepository.findAll(specification);
    }

    public List<BorrowRepayment> findList(Specification<BorrowRepayment> specification, Sort sort){
        return borrowRepaymentRepository.findAll(specification,sort);
    }

    public List<BorrowRepayment> findList(Specification<BorrowRepayment> specification, Pageable pageable){
        return borrowRepaymentRepository.findAll(specification,pageable).getContent();
    }

    public long count(Specification<BorrowRepayment> specification){
        return borrowRepaymentRepository.count(specification);
    }

}
