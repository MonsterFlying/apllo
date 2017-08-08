package com.gofobao.framework.windmill.borrow.service.impl;

import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.asset.service.AssetService;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.tender.contants.TenderConstans;
import com.gofobao.framework.tender.entity.Tender;
import com.gofobao.framework.tender.repository.TenderRepository;
import com.gofobao.framework.windmill.borrow.service.WindmillStatisticsService;
import com.gofobao.framework.windmill.borrow.vo.response.ByDayStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2017/8/3.
 */
@Component
public class WindmillStatisticsServiceImpl implements WindmillStatisticsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TenderRepository tenderRepository;



    @Autowired
    private AssetService assetService;

    @Override
    public ByDayStatistics bySomeDayStatistics(String date) {
        ByDayStatistics byDayStatistics = new ByDayStatistics();
        //縂待收本金
        String sumWaitPrincipalSql = " SELECT SUM(s.qdWaitRepayPrincipalTotal),SUM (s.jzWaitRepayPrincipalTotal),SUM (s.tjWaitRepayPrincipalTotal) FROM Statistic  s ";
        Query sumWaitPrincipalQuery = entityManager.createQuery(sumWaitPrincipalSql);

        List sumWaitPrincipalResult = sumWaitPrincipalQuery.getResultList();
        final Long[] sumWaitPrincipal = {0L};
        //装配结果集
        sumWaitPrincipalResult.stream().forEach(p -> {
            Object[] objects = (Object[]) p;
            sumWaitPrincipal[0] += Long.valueOf(objects[0].toString()) + Long.valueOf(objects[1].toString()) + Long.valueOf(objects[2].toString());
        });
        byDayStatistics.setAll_wait_back_money(StringHelper.formatDouble(sumWaitPrincipal[0] / 100D, false));

        //投资总额
        String todayTenderSumSql = "SELECT SUM(t.validMoney) FROM Tender t WHERE DATE_FORMAT(t.createdAt,'%Y-%m-%d')='"+date+"' AND t.status=:status";
        Query todayTenderQuery = entityManager.createQuery(todayTenderSumSql, Long.class);
        todayTenderQuery.setParameter("status", TenderConstans.SUCCESS);
        List<Long> tenderMoneySum = todayTenderQuery.getResultList();
        if(!StringUtils.isEmpty(tenderMoneySum.get(0))){
            byDayStatistics.setInvest_all_money(StringHelper.formatDouble(tenderMoneySum.get(0) / 100D, false));
        }

        Date date1 = DateHelper.stringToDate(date, DateHelper.DATE_FORMAT_YMD);

        Specification<Tender> specification = Specifications.<Tender>and()
                .between("createdAt", new Range<>(DateHelper.beginOfDate(date1), DateHelper.endOfDate(date1)))
                .eq("status", TenderConstans.SUCCESS)
                .build();

        List<Tender> tenders = tenderRepository.findAll(specification);
        if(!CollectionUtils.isEmpty(tenders)) {
            //投资人士
            Long lendCount = tenders.stream().map(p -> p.getId()).count();
            byDayStatistics.setLend_count(lendCount);
            //借款人数
            Long borrowCount = tenders.stream().map(p -> p.getBorrowId()).distinct().count();
            byDayStatistics.setBorrow_count(borrowCount);
        }
        byDayStatistics.setRetcode(VoBaseResp.OK);
        byDayStatistics.setRetmsg("查询成功");
        return byDayStatistics;
    }


}
