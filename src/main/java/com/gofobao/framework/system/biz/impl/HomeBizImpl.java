package com.gofobao.framework.system.biz.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.borrow.entity.Borrow;
import com.gofobao.framework.borrow.service.BorrowService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.finance.entity.FinancePlan;
import com.gofobao.framework.finance.service.FinancePlanService;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.system.biz.HomeBiz;
import com.gofobao.framework.system.biz.StatisticBiz;
import com.gofobao.framework.system.service.BannerService;
import com.gofobao.framework.system.vo.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@Slf4j
public class HomeBizImpl implements HomeBiz {

    @Autowired
    BannerService bannerService;

    @Autowired
    StatisticBiz statisticBiz;

    @Autowired
    BorrowService borrowService;
    @Autowired
    private FinancePlanService financePlanService;

    @Override
    public ResponseEntity<VoIndexResp> home() {
        VoIndexResp result = VoBaseResp.ok("查询成功", VoIndexResp.class);
        result.setBannerList(bannerService.index("mobile")); //获取bannar图
        try {
            NewIndexStatisics newIndexStatisics = statisticBiz.queryMobileIndexData();  // 获取首页统计
            result.setNewIndexStatisics(newIndexStatisics);
            Borrow borrow = borrowService.findNoviceBorrow();
            IndexBorrow indexBorrow = new IndexBorrow();
            if (borrow.getId() != 0L) {
                indexBorrow.setApr(StringHelper.formatMon(borrow.getApr() / 100d));   // 年化收益
                indexBorrow.setBorrowId(borrow.getId());
                indexBorrow.setLimit(String.valueOf(borrow.getTimeLimit()));
                indexBorrow.setTitle(borrow.getName());
                indexBorrow.setStartLimit(String.valueOf(new Double(borrow.getLowest() / 100D).longValue()));
            } else {  // 默认
                indexBorrow.setApr("*");
                indexBorrow.setBorrowId(0L);
                indexBorrow.setLimit("*");
                indexBorrow.setTitle("*****");
                indexBorrow.setStartLimit("*");
            }
            result.setIndexBorrow(indexBorrow);
        } catch (Exception e) {
            log.error("首页异常", e);
        }
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<VoFinanceIndexResp> financeHome() {
        VoFinanceIndexResp result = VoBaseResp.ok("查询成功", VoFinanceIndexResp.class);
        result.setBannerList(bannerService.index("mobile")); //获取bannar图
        try {
            NewIndexStatisics newIndexStatisics = statisticBiz.queryMobileIndexData();  // 获取首页统计
            result.setNewIndexStatisics(newIndexStatisics);
            Specification<FinancePlan> fps = Specifications
                    .<FinancePlan>and()
                    .notIn("status", 2, 4, 5)
                    .build();
            List<FinancePlan> financePlanList = financePlanService.findList(fps, new PageRequest(0, 1,
                    new Sort(new Sort.Order(Sort.Direction.ASC, "status"), new Sort.Order(Sort.Direction.DESC, "createdAt"))));
            FinanceIndexBorrow indexBorrow = new FinanceIndexBorrow();

            if (!CollectionUtils.isEmpty(financePlanList)) {
                FinancePlan financePlan = financePlanList.get(0);
                indexBorrow.setApr(StringHelper.formatMon(financePlan.getBaseApr() / 100d));   // 年化收益
                indexBorrow.setPlanId(financePlan.getId());
                indexBorrow.setLimit(String.valueOf(financePlan.getTimeLimit()));
                indexBorrow.setTitle(financePlan.getName());
                indexBorrow.setStartLimit(String.valueOf(new Double(financePlan.getLowest() / 100D).longValue()));
            } else {  // 默认
                indexBorrow.setApr("*");
                indexBorrow.setPlanId(0L);
                indexBorrow.setLimit("*");
                indexBorrow.setTitle("*****");
                indexBorrow.setStartLimit("*");
            }

            result.setFinanceIndexBorrow(indexBorrow);
        } catch (Exception e) {
            log.error("首页异常", e);
        }
        return ResponseEntity.ok(result);
    }
}
