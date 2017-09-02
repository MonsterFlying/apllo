package com.gofobao.framework.scheduler;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.contants.RepaymentContants;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.LtSpecification;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import com.gofobao.framework.tender.contants.BorrowContants;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
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
    private EntityManager entityManager;


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

    private void borrowRepay() {
        log.info("");
        Specification<BorrowRepayment> brs = Specifications
                .<BorrowRepayment>and()
                .eq("status", 0)
                .predicate(new LtSpecification("repayAt", new DataObject(DateHelper.beginOfDate(DateHelper.addDays(new Date(), 1)))))
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
            pageable = new PageRequest(pageIndex++, pageSize, new Sort(Sort.Direction.ASC, "id"));
            borrowRepaymentList = borrowRepaymentService.findList(brs, pageable);
            for (BorrowRepayment borrowRepayment : borrowRepaymentList) {
                borrowIds.add(borrowRepayment.getBorrowId());
            }

            bs = Specifications
                    .<Borrow>and()
                    .in("id", borrowIds.toArray())
                    .build();
            borrowList = borrowService.findList(bs);
            for (BorrowRepayment borrowRepayment : borrowRepaymentList) {
                for (Borrow borrow : borrowList) {
                    if (borrow.getType().intValue() != 1) {
                        continue;
                    }
                    if (String.valueOf(borrowRepayment.getBorrowId()).equals(String.valueOf(borrow.getId()))) {
                        try {
                            VoRepayReq voRepayReq = new VoRepayReq();
                            voRepayReq.setRepaymentId(borrowRepayment.getId());
                            voRepayReq.setUserId(borrowRepayment.getUserId());
                            voRepayReq.setInterestPercent(1d);
                            voRepayReq.setIsUserOpen(false);
                            repaymentBiz.newRepay(voRepayReq);
                        } catch (Exception e) {
                            log.error("borrowRepayScheduler error:", e);
                        }
                    }
                }
            }

        } while (borrowRepaymentList.size() >= pageSize);

    }

    /**
     *
     */
    @Scheduled(cron = "0 30 9 ? * *")
    public void todayRepayment() {
        log.info("自动还款调度启动");
        Date nowDate = new Date();
        String sqlStr = "SELECT r.* FROM  gfb_borrow_repayment r " +
                "LEFT JOIN " +
                "gfb_borrow b " +
                "ON " +
                "b.id=r.borrow_id  " +
                "WHERE " +
                "r.status=:status " +
                "AND  " +
                "r.repay_at<=:repayAt " +
                "AND " +
                "b.product_id IS NOT NULL " +
                "AND " +
                "(b.type=:type1 OR b.type=:type2)";
        Query query = entityManager.createNativeQuery(sqlStr, BorrowRepayment.class);
        query.setParameter("status", RepaymentContants.STATUS_NO);
        query.setParameter("repayAt", DateHelper.dateToString(DateHelper.endOfDate(nowDate)));
        query.setParameter("type1", BorrowContants.CE_DAI);
        query.setParameter("type2", BorrowContants.QU_DAO);
        List<BorrowRepayment> repayments = query.getResultList();
        if (!CollectionUtils.isEmpty(repayments)) {
            repayments.forEach(p -> {
                VoRepayReq voRepayReq = new VoRepayReq();
                voRepayReq.setRepaymentId(p.getId());
                voRepayReq.setUserId(p.getUserId());
                voRepayReq.setIsUserOpen(false);
                try {
                    repaymentBiz.newRepay(voRepayReq);
                    log.info(String.format("调度还款成功：打印还款期数信息:%s", new Gson().toJson(p)));
                } catch (Exception e) {
                    log.error("调度还款失败原因", e);
                    log.error(String.format("调度还款失败： 打印应款期数信息:%s", new Gson().toJson(p)));
                }
            });
        } else {
            log.info("今日没有要还款的批次！");
        }
    }
}
