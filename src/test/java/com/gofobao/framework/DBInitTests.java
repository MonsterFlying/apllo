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
import com.gofobao.framework.tender.vo.response.VoBorrowTenderUser;
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

import java.util.Collections;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j

public class DBInitTests {
    @Autowired
    BorrowService borrowService ;

    @Autowired
    BorrowRepaymentService borrowRepaymentService ;

    @Autowired
    TenderService tenderService ;

    @Autowired
    BorrowCollectionService borrowCollectionService ;

    @Test
    public void contextLoads() {
        int pageSize = 30;
        int pageIndex = 1 ;
        // 查询标的记录

        do{
            Specification<Borrow> borrowSpecification =  Specifications.<Borrow>and()
                    .build();
            Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.ASC, "id"))) ;
            List<Borrow> borrowList = borrowService.findList(borrowSpecification, pageable);
            if(CollectionUtils.isEmpty(borrowList)){
                break;
            }

            // 查询标的记录信息
            for(Borrow borrow : borrowList){
                String borrowName = borrow.getName() ;  // 标的信息
                int repayTotal = borrow.getRepayFashion().equals(1) ? 1 : borrow.getTimeLimit() ;  // 还款期数
                long borrowId = borrow.getId() ;
                int borrowState  = 0;
                if(borrow.getSuccessAt() == null ){
                    borrowState = 0 ;
                }else if(borrow.getCloseAt() != null) {
                    borrowState = 2 ;
                }else{
                    borrowState = 1 ;
                }


                Long repaymentUserId = borrow.getUserId() ;

                // 查询还款记录
                Specification<BorrowRepayment> borrowRepaymentSpecification =  Specifications.<BorrowRepayment>and()
                        .eq("borrowId",  borrowId)
                        .build();
                List<BorrowRepayment> borrowRepaymentList = borrowRepaymentService.findList(borrowRepaymentSpecification);
                if(!CollectionUtils.isEmpty(borrowRepaymentList)){
                    for(BorrowRepayment borrowRepayment : borrowRepaymentList){
                        borrowRepayment.setUserId(repaymentUserId);
                        borrowRepaymentService.save(borrowRepayment) ;
                    }
                }



                // 查询投标信息
                Specification<Tender> tenderSpecification =  Specifications.<Tender>and()
                        .eq("borrowId",  borrowId)
                        .build();
                List<Tender> tenderList = tenderService.findList(tenderSpecification);
                if(!CollectionUtils.isEmpty(tenderList)){
                    for (Tender tender: tenderList){
                        long tenderUserId = tender.getUserId() ;
                        long tenderId = tender.getId() ;
                        tender.setStatus(borrowState);
                        tenderService.insert(tender) ;

                        // 查询回款记录

                        // 查询投标信息
                        Specification<BorrowCollection> borrowCollectionSpecification =  Specifications.<BorrowCollection>and()
                                .eq("tenderId",  tenderId)
                                .build();

                    }
                }





            }












            pageIndex++ ;
        }while (true) ;

    }



}
