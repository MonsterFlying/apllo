package com.gofobao.framework.scheduler;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.vo.request.VoAdvanceReq;
import com.gofobao.framework.system.service.BannerService;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Zeke on 2017/7/11.
 */
@Component
@Slf4j
public class BorrowAdvanceScheduler {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private BorrowService borrowService;
    @Autowired
    private RepaymentBiz repaymentBiz;

    @Scheduled(cron = "0 55 23 * * ? ")
    public void process() {
        List<BorrowRepayment> borrowRepaymentList = null;
        List<Borrow> borrowList = null;
        List<Long> borrowIds = null;
        Specification<Borrow> bs = null;
        int pageIndex = 0;
        int pageSize = 50;

        StringBuffer sql = new StringBuffer("select * from gfb_borrow_repayment where ");
        sql.append(" repay_at < " + DateHelper.dateToString(DateHelper.beginOfDate(DateHelper.subDays(new Date(), 1))));
        sql.append(" and advance_at_yes is null ");
        sql.append(" order by id");

        do {
            borrowIds = new ArrayList<>();
            Query query = entityManager.createNativeQuery(sql.toString(), BorrowRepayment.class);
            query.setFirstResult(pageIndex++);
            query.setMaxResults(pageSize);
            borrowRepaymentList = query.getResultList();
            for (BorrowRepayment borrowRepayment : borrowRepaymentList) {
                borrowIds.add(borrowRepayment.getBorrowId());
            }

            bs = Specifications
                    .<Borrow>and()
                    .in("id", borrowIds.toArray())
                    .build();
            borrowList = borrowService.findList(bs);

            for (BorrowRepayment borrowRepayment : borrowRepaymentList) {
                for (Borrow borrow : borrowList){
                    if (String.valueOf(borrowRepayment.getBorrowId()).equals(String.valueOf(borrow.getId()))){
                        VoAdvanceReq voAdvanceReq = new VoAdvanceReq();
                        voAdvanceReq.setRepaymentId(borrowRepayment.getId());
                        try {
                            repaymentBiz.advance(voAdvanceReq);
                        } catch (Exception e) {
                            log.error("borrowAdvanceScheduler error:",e);
                        }
                    }
                }
            }
        } while (borrowRepaymentList.size() >= pageSize);
    }
}
