package com.gofobao.framework.scheduler;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.entity.Asset;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.common.data.DataObject;
import com.gofobao.framework.common.data.LtSpecification;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.repayment.biz.LoanBiz;
import com.gofobao.framework.repayment.biz.RepaymentBiz;
import com.gofobao.framework.repayment.entity.BorrowRepayment;
import com.gofobao.framework.repayment.service.BorrowRepaymentService;
import com.gofobao.framework.repayment.vo.request.VoRepayReq;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.Map;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Autowired
    private LoanBiz loanBiz;

    @Autowired
    private AssetService assetService;


    //@Scheduled(cron = "0 50 23 * * ? ")
    public void process() {
        borrowRepay();
    }

    /**
     * 发送还款短信站内信提醒
     */
    public void sendRepayMassage() {
        //规则：
    }

    //@Transactional(rollbackOn = Exception.class)
    private void borrowRepay() {
        log.info("进入批次还款任务调度");
        long repayUserId = 22002;/*官标由zfh还款*/
        int pageIndex = 0;
        int pageSize = 50;
        List<BorrowRepayment> borrowRepaymentList = null;
        /* 查询未回款转让 */
        Specification<BorrowRepayment> brs = Specifications
                .<BorrowRepayment>and()
                .eq("status", 0)
                .predicate(new LtSpecification("repayAt", new DataObject(DateHelper.beginOfDate(DateHelper.addDays(new Date(), 1)))))
                .build();
        do {
            /* 还款记录集合 */
            borrowRepaymentList = borrowRepaymentService.findList(brs, new PageRequest(pageIndex++, pageSize));
            /* borrowId集合 */
            Set<Long> borrowIds = borrowRepaymentList.stream().map(BorrowRepayment::getBorrowId).collect(Collectors.toSet());
            //查询borrow记录
            Specification<Borrow> bs = Specifications
                    .<Borrow>and()
                    .in("id", borrowIds.toArray())
                    .build();
            /* 借款记录集合 */
            List<Borrow> borrowList = borrowService.findList(bs);
            Map<Long/*borrowId*/, Borrow> borrowMap = borrowList.stream().collect(Collectors.toMap(Borrow::getId, Function.identity()));
            /* 登记还款人id */
            Set<Long> registerUserIds = borrowRepaymentList.stream().map(BorrowRepayment::getUserId).collect(Collectors.toSet());
            registerUserIds.add(repayUserId);
            //查询用户存管记录
            Specification<Asset> as = Specifications
                    .<Asset>and()
                    .in("userId", registerUserIds.toArray())
                    .build();
            /* 用户资产记录集合 */
            List<Asset> assetList = assetService.findList(as);
            Map<Long/*userId*/, Asset> assetMap = assetList.stream().collect(Collectors.toMap(Asset::getUserId, Function.identity()));
            for (BorrowRepayment borrowRepayment : borrowRepaymentList) {
                Borrow borrow = borrowMap.get(borrowRepayment.getBorrowId());
                Asset asset = null;
                ImmutableSet<Long> borrowType = ImmutableSet.of(0l, 4l);
                if (borrowType.contains(borrow)) {//如果是官标由zfh还款
                    asset = assetMap.get(repayUserId);
                } else {
                    asset = assetMap.get(borrowRepayment.getUserId());
                }
                //（初步）判断可用金额是否大于还款金额
                if (asset.getUseMoney() < borrowRepayment.getRepayMoney()) {
                    try {
                        VoRepayReq voRepayReq = new VoRepayReq();
                        voRepayReq.setRepaymentId(borrowRepayment.getId());
                        voRepayReq.setUserId(asset.getUserId());
                        voRepayReq.setInterestPercent(1d);
                        voRepayReq.setIsUserOpen(false);
                        repaymentBiz.newRepay(voRepayReq);
                    } catch (Exception e) {
                        log.error("borrowRepayScheduler error:", e);
                    }
                }
            }
        } while (borrowRepaymentList.size() >= pageSize);

    }


    /**
     * 每天早上9点 调度还款当日所需要还款的的官标
     */
    //@Scheduled(cron = "0 00 23 * * ? ")
    // @Transactional(rollbackOn = Exception.class)
    public void todayRepayment() {
        log.info("自动还款调度启动");
        loanBiz.timingRepayment(new Date());
    }
}
