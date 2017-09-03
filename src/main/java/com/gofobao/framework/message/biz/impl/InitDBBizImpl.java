package com.gofobao.framework.message.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.message.biz.InitDBBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InitDBBizImpl implements InitDBBiz {

    @Autowired
    BorrowService borrowService;

    @Autowired
    BorrowRepaymentService borrowRepaymentService;

    @Autowired
    TenderService tenderService;

    @Autowired
    BorrowCollectionService borrowCollectionService;

    @Override
    public void initDb() {
        int borrowCount = 1;
        int pageSize = 1000, pageIndex = 0, realSize = 0;
        Date nowDate = new Date();
        int loop = 1;
        do {
            log.info("=========================================");
            log.info("进入主循环");
            log.info("=========================================");
            ImmutableList<Integer> avableStatus = ImmutableList.of(1, 3); // 保函招标中, 满标复审通过
            Specification<Borrow> borrowSpecification = Specifications.<Borrow>and()
                    .in("status", avableStatus.toArray())
                    .build();
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.ASC, "id")));
            List<Borrow> borrowList = borrowService.findList(borrowSpecification, pageable);
            if (CollectionUtils.isEmpty(borrowList)) {
                break;
            }

            realSize = borrowList.size();
            pageIndex++;

            List<Tender> tenderDateCache = new ArrayList<>();
            List<BorrowRepayment> borrowRepaymentDataCache = new ArrayList<>();
            List<BorrowCollection> borrowCollectionDateCache = new ArrayList<>();
            Set<Long> borrowIdSet = borrowList.stream().map(borrow -> borrow.getId()).collect(Collectors.toSet());  // 标的Id集合

            List<BorrowRepayment> borrowRepaymentListByBorrowId = findRepaymentListByBorrowId(borrowIdSet);   // 当前标的所有的还款
            Map<Long, List<BorrowRepayment>> borrowRepaymentAndBorrowIdRefMap =
                    borrowRepaymentListByBorrowId.stream().collect(Collectors.groupingBy(BorrowRepayment::getBorrowId));

            List<Tender> tenderListByBorrowId = findTenderByBorrowIds(borrowIdSet);  // 投标记录
            Map<Long, List<Tender>> tenderAndBorrowIdRefMap = tenderListByBorrowId.stream().collect(Collectors.groupingBy(Tender::getBorrowId));
            Set<Long> tenderIdSet = tenderListByBorrowId.stream().map(tender -> tender.getId()).collect(Collectors.toSet());  // 投标记录ID

            List<BorrowCollection> borrowCollectionListAll = findBorrowCollectionByTenderId(tenderIdSet);
            Map<Long, List<BorrowCollection>> borrowCollectionAndTenderIdRefMap = borrowCollectionListAll.stream().collect(Collectors.groupingBy(BorrowCollection::getTenderId));

            for (Borrow borrow : borrowList) {
                log.info("以迁移标的数量:" + (++borrowCount));
                long borrowId = borrow.getId();
                int tenderState = caculTenderState(borrow);  // 计算当前标的状态
                Long borrowUserId = borrow.getUserId();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentAndBorrowIdRefMap.get(borrowId);
                if (!CollectionUtils.isEmpty(borrowRepaymentList)) {
                    for (BorrowRepayment borrowRepayment : borrowRepaymentList) {
                        borrowRepayment.setUserId(borrowUserId);
                        borrowRepayment.setUpdatedAt(nowDate);
                    }
                    borrowRepaymentDataCache.addAll(borrowRepaymentList);
                }

                List<Tender> tenderList = tenderAndBorrowIdRefMap.get(borrowId);
                if (!CollectionUtils.isEmpty(tenderList)) {
                    for (Tender tender : tenderList) {
                        long tenderUserId = tender.getUserId();
                        long tenderId = tender.getId();
                        tender.setState(tenderState);
                        tender.setUpdatedAt(nowDate);

                        List<BorrowCollection> borrowCollectionList = borrowCollectionAndTenderIdRefMap.get(tenderId); // 查询回款记录
                        if (!CollectionUtils.isEmpty(borrowCollectionList)) {
                            for (BorrowCollection borrowCollection : borrowCollectionList) {
                                borrowCollection.setUserId(tenderUserId);
                                borrowCollection.setBorrowId(borrowId);
                                borrowCollection.setUpdatedAt(nowDate);
                            }

                            borrowCollectionDateCache.addAll(borrowCollectionList);
                        }
                    }
                    tenderDateCache.addAll(tenderList);
                }
            }

            tenderService.save(tenderDateCache);
            borrowRepaymentService.save(borrowRepaymentDataCache);
            borrowCollectionService.save(borrowCollectionDateCache);

            log.info("第一次调度完成" + (++loop));
        } while (realSize == pageSize);

    }


    private int caculTenderState(Borrow borrow) {
        int tenderState = -1;
        if (borrow.getStatus() == 1) {  // 招标中
            tenderState = 1;  // 投标中
        } else if (borrow.getStatus() == 3) {
            if (!ObjectUtils.isEmpty(borrow.getCloseAt())) {
                tenderState = 3;  // 已结清
            } else if (!ObjectUtils.isEmpty(borrow.getSuccessAt())) {
                tenderState = 2;  // 回款中
            } else {
                tenderState = -2;  // 特殊请款
            }
        }
        return tenderState;
    }

    private List<BorrowCollection> findBorrowCollectionByTenderId(Set<Long> tenderIdSet) {
        Specification<BorrowCollection> borrowCollectionSpecification = Specifications.<BorrowCollection>and()
                .in("tenderId", tenderIdSet.toArray())
                .build();

        return borrowCollectionService.findList(borrowCollectionSpecification);
    }

    private List<Tender> findTenderByBorrowIds(Set<Long> borrowIdSet) {
        // 查询投标信息
        Specification<Tender> tenderSpecification = Specifications.<Tender>and()
                .in("borrowId", borrowIdSet.toArray())
                .eq("status", 1) // 成功的
                .build();

        return tenderService.findList(tenderSpecification);
    }

    private List<BorrowRepayment> findRepaymentListByBorrowId(Set<Long> borrowIdSet) {
        Specification<BorrowRepayment> borrowRepaymentSpecification = Specifications.<BorrowRepayment>and()
                .in("borrowId", borrowIdSet.toArray())
                .build();

        return borrowRepaymentService.findList(borrowRepaymentSpecification);
    }
}
