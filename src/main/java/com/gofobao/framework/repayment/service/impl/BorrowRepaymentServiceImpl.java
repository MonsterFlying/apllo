package com.gofobao.framework.repayment.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.repository.BorrowRepository;
import com.gofobao.framework.collection.vo.request.VoCollectionOrderReq;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderList;
import com.gofobao.framework.collection.vo.response.VoViewCollectionOrderRes;
import com.gofobao.framework.collection.vo.response.VoViewOrderDetailRes;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.repayment.contants.RepaymentContants;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.repository.BorrowRepaymentRepository;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoInfoReq;
import com.gofobao.framework.repayment.vo.response.RepayCollectionLog;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by admin on 2017/6/1.
 */
@Component
public class BorrowRepaymentServiceImpl implements BorrowRepaymentService {

    @Autowired
    private BorrowRepaymentRepository borrowRepaymentRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @PersistenceContext
    private EntityManager entityManager;


    /**
     * 还款计划列表
     *
     * @param voCollectionOrderReq
     * @return VoViewCollectionOrderListRes
     */
    @Override
    public VoViewCollectionOrderList repaymentList(VoCollectionOrderReq voCollectionOrderReq) {
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
        VoViewCollectionOrderList orderListRes = new VoViewCollectionOrderList();
        List<VoViewCollectionOrderRes> orderResList = new ArrayList<>();

        repaymentList.stream().forEach(p -> {
            VoViewCollectionOrderRes collectionOrderRes = new VoViewCollectionOrderRes();
            Borrow borrow = borrowMap.get(p.getId());
            collectionOrderRes.setBorrowName(borrow.getName());
            collectionOrderRes.setOrder(p.getOrder() + 1);
            collectionOrderRes.setCollectionMoneyYes(StringHelper.formatMon(p.getRepayMoneyYes() / 100d));
            collectionOrderRes.setCollectionMoney(StringHelper.formatMon(p.getRepayMoney() / 100d));
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
        orderListRes.setSumCollectionMoneyYes(StringHelper.formatMon(moneyYesSum / 100d));
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
        detailRes.setInterest(StringHelper.formatMon(interest / 100d));
        detailRes.setPrincipal(StringHelper.formatMon(principal / 100d));
        detailRes.setBorrowName(borrow.getName());
        detailRes.setCollectionMoney(StringHelper.formatMon(borrowRepayment.getRepayMoneyYes() / 100d));
        detailRes.setLateDays(borrowRepayment.getLateDays());
        detailRes.setOrder(borrowRepayment.getOrder() + 1);
        detailRes.setStartAt(DateHelper.dateToString(borrowRepayment.getRepayAtYes()));
        return detailRes;
    }


    @Override
    public List<Integer> days(Long userId, String time) {
        String sql = "SELECT DAY(repay_at) FROM gfb_borrow_repayment " +
                "where " +
                "user_id=" + userId + " " +
                "and " +
                "`status`=0 " +
                "and   date_format(repay_at,'%Y%m') =" + time +
                " GROUP BY  day(repay_at)";
        Query query = entityManager.createNativeQuery(sql);
        List result = query.getResultList();
        return result;
    }

    @Override
    public List<RepayCollectionLog> logs(Long borrowId) {
        List<BorrowRepayment> repaymentList = borrowRepaymentRepository.findByBorrowId(borrowId);
        if (CollectionUtils.isEmpty(repaymentList)) {
            return Collections.EMPTY_LIST;
        }
        List<RepayCollectionLog> logList = Lists.newArrayList();
        repaymentList.stream().forEach(p -> {
            RepayCollectionLog log = new RepayCollectionLog();
            log.setInterest(StringHelper.formatMon(p.getInterest() / 100d));
            log.setLateInterest(StringHelper.formatMon(p.getLateDays() / 100d));
            log.setOrder(p.getOrder() + 1);
            log.setPrincipal(StringHelper.formatMon(p.getPrincipal() / 100d));
            log.setRepayAt(DateHelper.dateToString(p.getRepayAt()));
            log.setRepayMoney(StringHelper.formatMon(p.getRepayMoney() / 100d));
            log.setRepayMoneyYes(StringHelper.formatMon(p.getRepayMoneyYes() / 100d));
            if (p.getStatus() == RepaymentContants.STATUS_NO) { //未还款
                log.setRepayAtYes("---");
                log.setRemark("---");
            } else {
                String date = DateHelper.dateToString(p.getRepayAtYes());
                log.setRepayAtYes(date);
                log.setRemark(date + RepaymentContants.STATUS_YES_STR);
            }
            logList.add(log);
        });
        return logList;
    }

    public BorrowRepayment save(BorrowRepayment borrowRepayment) {
        return borrowRepaymentRepository.save(borrowRepayment);
    }

    public BorrowRepayment insert(BorrowRepayment borrowRepayment) {
        if (ObjectUtils.isEmpty(borrowRepayment)) {
            return null;
        }
        borrowRepayment.setId(null);
        return borrowRepaymentRepository.save(borrowRepayment);
    }

    public BorrowRepayment updateById(BorrowRepayment borrowRepayment) {
        if (ObjectUtils.isEmpty(borrowRepayment) || ObjectUtils.isEmpty(borrowRepayment.getId())) {
            return null;
        }
        return borrowRepaymentRepository.save(borrowRepayment);
    }


    public BorrowRepayment findByIdLock(Long id) {
        return borrowRepaymentRepository.findById(id);
    }

    public BorrowRepayment findById(Long id) {
        return borrowRepaymentRepository.findOne(id);
    }

    public List<BorrowRepayment> findList(Specification<BorrowRepayment> specification) {
        return borrowRepaymentRepository.findAll(specification);
    }

    public List<BorrowRepayment> findList(Specification<BorrowRepayment> specification, Sort sort) {
        return borrowRepaymentRepository.findAll(specification, sort);
    }

    public List<BorrowRepayment> findList(Specification<BorrowRepayment> specification, Pageable pageable) {
        return borrowRepaymentRepository.findAll(specification, pageable).getContent();
    }

    public long count(Specification<BorrowRepayment> specification) {
        return borrowRepaymentRepository.count(specification);
    }

}
