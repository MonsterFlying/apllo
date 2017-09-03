package com.gofobao.framework;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.collection.entity.BorrowCollection;
import com.gofobao.framework.collection.service.BorrowCollectionService;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j

public class DBInitTests {
    @Autowired
    BorrowService borrowService;

    @Autowired
    BorrowRepaymentService borrowRepaymentService;

    @Autowired
    TenderService tenderService;

    @Autowired
    BorrowCollectionService borrowCollectionService;

    @Test
    public void initDatabase() {
        int pageSize = 2000, pageIndex = 0, realSize = 0;
        // 查询标的记录
        do {
            log.info("=========================================");
            log.info("进入主循环");
            log.info("=========================================");
            ImmutableList<Integer> avableStatus = ImmutableList.of(1, 3) ; // 保函招标中, 满标复审通过
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

            List<Tender> allTender = new ArrayList<>() ;
            List<BorrowRepayment> allBorrowRepaymentList = new ArrayList<>() ;
            List<BorrowCollection> allBorrowCollectionList = new ArrayList<>() ;
            Set<Long> borrowIdSet = borrowList.stream().map(borrow -> borrow.getId()).collect(Collectors.toSet());  // 标的Id集合

            List<BorrowRepayment> borrowRepaymentsAll = findRepaymentListByBorrowId(borrowIdSet);   // 当前标的所有的还款
            Map<Long, List<BorrowRepayment>> borrowRepaymentAndBorrowIdRefMap =
                    borrowRepaymentsAll.stream().collect(Collectors.groupingBy(BorrowRepayment::getBorrowId));


            // 查询投标信息
            Specification<Tender> tenderSpecification = Specifications.<Tender>and()
                    .in("borrowId", borrowIdSet.toArray())
                    .eq("status", 1) // 成功的
                    .build();

            List<Tender> tenderAll = tenderService.findList(tenderSpecification) ;
            Map<Long, List<Tender>> tenderMap = tenderAll.stream().collect(Collectors.groupingBy(Tender::getBorrowId));
            Set<Long> tenderIdSet = tenderAll.stream().map(tender -> tender.getId()).collect(Collectors.toSet());

            Specification<BorrowCollection> borrowCollectionSpecification = Specifications.<BorrowCollection>and()
                    .in("tenderId", tenderIdSet.toArray())
                    .build();

            List<BorrowCollection> borrowCollectionListAll = borrowCollectionService.findList(borrowCollectionSpecification);
            Map<Long, List<BorrowCollection>> borrowCollectionMap = borrowCollectionListAll.stream().collect(Collectors.groupingBy(BorrowCollection::getTenderId));


            for (Borrow borrow : borrowList) {
                log.info("=========================================");
                log.info("进入标循环" + borrow.getId());
                log.info("=========================================");
                long borrowId = borrow.getId();
                int borrowState;
                if( borrow.getCloseAt() != null){
                    borrowState = 3 ;  // 已结清
                }else {
                    if(borrow.getSuccessAt() != null && borrow.getStatus() == 3){
                        borrowState = 2 ; // 回款中
                    }else{
                        borrowState = 1 ; // 投标中
                    }
                }
                Long repaymentUserId = borrow.getUserId();
                // 查询还款记录
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentAndBorrowIdRefMap.get(borrowId);
                if (!CollectionUtils.isEmpty(borrowRepaymentList)) {
                    for (BorrowRepayment borrowRepayment : borrowRepaymentList) {
                        borrowRepayment.setUserId(repaymentUserId);
                    }
                    allBorrowRepaymentList.addAll(borrowRepaymentList) ;
                }

                List<Tender> tenderList = tenderMap.get(borrowId);
                if (!CollectionUtils.isEmpty(tenderList)) {
                    for (Tender tender : tenderList) {
                        long tenderUserId = tender.getUserId();
                        long tenderId = tender.getId();
                        tender.setState(borrowState);

                        // 查询回款记录
                        List<BorrowCollection> borrowCollectionList = borrowCollectionMap.get(tenderId) ;
                        if (!CollectionUtils.isEmpty(borrowCollectionList)) {
                            for (BorrowCollection borrowCollection : borrowCollectionList) {
                                borrowCollection.setUserId(tenderUserId);
                                borrowCollection.setBorrowId(borrowId);
                            }
                            allBorrowCollectionList.addAll(borrowCollectionList) ;
                        }
                    }
                    allTender.addAll(tenderList) ;
                }
            }

            tenderService.save(allTender) ;
            borrowRepaymentService.save(allBorrowRepaymentList) ;
            borrowCollectionService.save(allBorrowCollectionList) ;
        } while (realSize == pageSize);
    }

    private List<BorrowRepayment> findRepaymentListByBorrowId(Set<Long> borrowIdSet) {
        Specification<BorrowRepayment> borrowRepaymentSpecification = Specifications.<BorrowRepayment>and()
                .in("borrowId", borrowIdSet.toArray())
                .build();

        return borrowRepaymentService.findList(borrowRepaymentSpecification);
    }
}
