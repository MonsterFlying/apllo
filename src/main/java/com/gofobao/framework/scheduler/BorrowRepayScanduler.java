package com.gofobao.framework.scheduler;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.biz.BorrowBiz;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.LtSpecification;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoInstantlyRepaymentReq;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/7/10.
 */
@Component
@Slf4j
public class BorrowRepayScanduler {

    @Autowired
    private BorrowRepaymentService borrowRepaymentService;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private RepaymentBiz repaymentBiz;

    @Scheduled(cron = "0 50 23 * * ? ")
    public void process() {
        borrowRepay();
    }

    @Scheduled(cron = "0 00 23 * * ? ")
    public void process01() {
        borrowRepay();
    }

    private void borrowRepay(){
        Specification<BorrowRepayment> brs = Specifications
                .<BorrowRepayment>and()
                .eq("status",0)
                .predicate(new LtSpecification("repayAt",new DataObject(DateHelper.beginOfDate(DateHelper.addDays(new Date(),1)))))
                .build();

        List<BorrowRepayment> borrowRepaymentList = null;
        List<Borrow> borrowList = null;
        List<Long> borrowIds = null;
        Specification<Borrow> bs = null;
        Pageable pageable = null;
        int pageIndex = 0;
        int pageSize = 50;
        do {
            borrowIds = new ArrayList<>();
            pageable = new PageRequest(pageIndex++,pageSize,new Sort(Sort.Direction.ASC,"id"));
            borrowRepaymentList = borrowRepaymentService.findList(brs,pageable);
            for (BorrowRepayment borrowRepayment : borrowRepaymentList){
                borrowIds.add(borrowRepayment.getBorrowId());
            }

            bs = Specifications
                    .<Borrow>and()
                    .in("id",borrowIds.toArray())
                    .build();
            borrowList = borrowService.findList(bs);
            for (BorrowRepayment borrowRepayment : borrowRepaymentList){
                for (Borrow borrow : borrowList){
                    if (String.valueOf(borrowRepayment.getBorrowId()).equals(String.valueOf(borrow.getId()))){
                        try {
                            VoRepayReq voRepayReq = new VoRepayReq();
                            voRepayReq.setRepaymentId(borrowRepayment.getId());
                            voRepayReq.setUserId(borrowRepayment.getUserId());
                            voRepayReq.setInterestPercent(0d);
                            voRepayReq.setIsUserOpen(false);
                            repaymentBiz.repay(voRepayReq);
                        } catch (Exception e) {
                            log.error("borrowRepayScheduler error:",e);
                        }
                    }
                }
            }

        }while (borrowRepaymentList.size() >= pageSize);
    }
}
