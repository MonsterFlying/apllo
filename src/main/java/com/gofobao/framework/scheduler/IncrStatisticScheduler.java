package com.gofobao.framework.scheduler;

import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.system.biz.IncrStatisticBiz;
import com.gofobao.framework.system.entity.IncrStatistic;
import com.gofobao.framework.system.entity.Statistic;
import com.gofobao.framework.system.service.IncrStatisticService;
import com.gofobao.framework.system.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/7/10.
 */
public class IncrStatisticScheduler {

    @Autowired
    private IncrStatisticService incrStatisticService;
    @Autowired
    private StatisticService statisticService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 30 0 * * ? ")
    public void process() {
        Date startDate = DateHelper.beginOfDate(DateHelper.subDays(new Date(), 1));
        Date endDate = DateHelper.endOfDate(startDate);

        StringBuffer sql = new StringBuffer("select sum(gfb_daily_asset.use_money) as useMoneySum, sum(gfb_daily_asset.no_use_money) as noUseMoneySum " +
                "from `gfb_users` left join `gfb_daily_asset` on `gfb_users`.`id` = `gfb_daily_asset`.`user_id`" +
                " where `gfb_users`.`id` not in (22) " +
                "and `gfb_users`.`type` <> 'borrower' " +
                "and `gfb_daily_asset`.`date` = '" + DateHelper.dateToString(startDate, DateHelper.DATE_FORMAT_YMD) + "' " +
                "limit 1");
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql.toString());
        if (CollectionUtils.isEmpty(resultList)) {
            return;
        }
        Map<String, Object> result = resultList.get(0);
        Long userMoneySum = NumberHelper.toLong(result.get("useMoneySum"));
        Long noUseMoneySum = NumberHelper.toLong(result.get("noUseMoneySum"));

        sql = new StringBuffer("select sum(`money`) as aggregate from `gfb_borrow` " +
                "where `type` = 0 and `tender_id` is null and `release_at` >= '" + DateHelper.dateToString(startDate) + "'" +
                " and `release_at` <= '" + DateHelper.dateToString(endDate) + "'");
        Map<String, Object> resultMap = jdbcTemplate.queryForMap(sql.toString());
        Long tjSumPublish = NumberHelper.toLong(resultMap.get("aggregate"));

        sql = new StringBuffer("select sum(`money`) as aggregate from `gfb_borrow` " +
                "where `type` = 1 and `release_at` >= '" + DateHelper.dateToString(startDate) + "' " +
                "and `release_at` <= '" + DateHelper.dateToString(endDate) + "'");
        resultMap = jdbcTemplate.queryForMap(sql.toString());
        Long jzSumPublish = NumberHelper.toLong(resultMap.get("aggregate"));

        sql = new StringBuffer("select sum(`money`) as aggregate from `gfb_borrow` " +
                "where `type` = 0 and `tender_id` > 0 and `release_at` >= '" + DateHelper.dateToString(startDate) + "' " +
                "and `release_at` <= '" + DateHelper.dateToString(endDate) + "'");
        resultMap = jdbcTemplate.queryForMap(sql.toString());
        Long lzSumPublish = NumberHelper.toLong(resultMap.get("aggregate"));

        sql = new StringBuffer("select sum(`money`) as aggregate from `gfb_borrow` where" +
                " `type` = 4 and `release_at` >= '" + DateHelper.dateToString(startDate) + "' and `release_at` <= '" + DateHelper.dateToString(endDate) + "'");
        resultMap = jdbcTemplate.queryForMap(sql.toString());
        Long qdSumPublish = NumberHelper.toLong(resultMap.get("aggregate"));

        sql = new StringBuffer("select sum(`money`) as aggregate from `gfb_borrow` where " +
                "`type` = 2 and `release_at` >= '" + DateHelper.dateToString(startDate) + "' " +
                "and `release_at` <= '" + DateHelper.dateToString(endDate) + "'");
        resultMap = jdbcTemplate.queryForMap(sql.toString());
        Long miaoSumPublish = NumberHelper.toLong(resultMap.get("aggregate"));

        sql = new StringBuffer("select sum(`money`) as aggregate from `gfb_borrow`" +
                " where `status` = 3 and `type` = 0 and `tender_id` is null " +
                "and `success_at` >= '" + DateHelper.dateToString(startDate) + "' " +
                "and `success_at` <= '" + DateHelper.dateToString(endDate) + "'");
        resultMap = jdbcTemplate.queryForMap(sql.toString());
        Long tjSumSuccess = NumberHelper.toLong(resultMap.get("aggregate"));

        sql = new StringBuffer("select sum(`money`) as aggregate from `gfb_borrow`" +
                " where `status` = 3 and `type` = 1 and" +
                " `success_at` >= '" + DateHelper.dateToString(startDate) + "' " +
                "and `success_at` <= '" + DateHelper.dateToString(endDate) + "'");
        resultMap = jdbcTemplate.queryForMap(sql.toString());
        Long jzSumSuccess = NumberHelper.toLong(resultMap.get("aggregate"));

        sql = new StringBuffer("select sum(`money`) as aggregate from `gfb_borrow`" +
                " where `status` = 3 and `type` = 0 and `tender_id` > 0 " +
                "and `success_at` >= '" + DateHelper.dateToString(startDate) + "' " +
                "and `success_at` <= '" + DateHelper.dateToString(endDate) + "'");
        resultMap = jdbcTemplate.queryForMap(sql.toString());
        Long lzSumSuccess = NumberHelper.toLong(resultMap.get("aggregate"));

        sql = new StringBuffer("select sum(`money`) as aggregate from `gfb_borrow`" +
                " where `status` = 3 and `type` = 2 " +
                "and `success_at` >= '" + DateHelper.dateToString(startDate) + "' " +
                "and `success_at` <= '" + DateHelper.dateToString(endDate) + "'");
        resultMap = jdbcTemplate.queryForMap(sql.toString());
        Long miaoSumSuccess = NumberHelper.toLong(resultMap.get("aggregate"));

        sql = new StringBuffer("select sum(`money`) as aggregate from `gfb_borrow`" +
                " where `status` = 3 and `type` = 4 " +
                "and `success_at` >= '" + DateHelper.dateToString(startDate) + "' " +
                "and `success_at` <= '" + DateHelper.dateToString(endDate) + "'");
        resultMap = jdbcTemplate.queryForMap(sql.toString());
        Long qdSumSuccess = NumberHelper.toLong(resultMap.get("aggregate"));

        Long tjSumRepay = 0l;
        Long tjSumRepayPrincipal = 0l;
        sql = new StringBuffer("select sum(repay_money_yes) as tjSumRepay, " +
                "sum(principal) as tjSumRepayPrincipal from `gfb_borrow_repayment` where `status` = 1 " +
                "and exists (select * from `gfb_borrow` where `gfb_borrow_repayment`.`borrow_id` = `gfb_borrow`.`id`" +
                " and `type` = 0 and `tender_id` is null) " +
                "and `repay_at_yes` >=  '" + DateHelper.dateToString(startDate) + "' " +
                "and `repay_at_yes` <=  '" + DateHelper.dateToString(endDate) + "' limit 1");
        resultMap = jdbcTemplate.queryForMap(sql.toString());
        if (!CollectionUtils.isEmpty(resultMap)) {
            tjSumRepay = NumberHelper.toLong(resultMap.get("tjSumRepay"));
            tjSumRepayPrincipal = NumberHelper.toLong(resultMap.get("tjSumRepayPrincipal"));
        }

        Long jzSumRepay = 0l;
        Long jzSumRepayPrincipal = 0l;
        sql = new StringBuffer("select sum(repay_money_yes) as jzSumRepay, " +
                "sum(principal) as jzSumRepayPrincipal from `gfb_borrow_repayment` where `status` = 1 " +
                "and exists (select * from `gfb_borrow` where `gfb_borrow_repayment`.`borrow_id` = `gfb_borrow`.`id`" +
                " and `type` = 1) and `repay_at_yes` >=  '" + DateHelper.dateToString(startDate) + "'  " +
                "and `repay_at_yes` <=  '" + DateHelper.dateToString(endDate) + "'  limit 1");
        resultMap = jdbcTemplate.queryForMap(sql.toString());
        if (!CollectionUtils.isEmpty(resultMap)) {
            jzSumRepay = NumberHelper.toLong(resultMap.get("jzSumRepay"));
            jzSumRepayPrincipal = NumberHelper.toLong(resultMap.get("jzSumRepayPrincipal"));
        }

        Long qdSumRepay = 0l;
        Long qdSumRepayPrincipal = 0l;
        sql = new StringBuffer("select sum(repay_money_yes) as qdSumRepay, " +
                "sum(principal) as qdSumRepayPrincipal from `gfb_borrow_repayment` where `status` = 1 " +
                "and exists (select * from `gfb_borrow` where `gfb_borrow_repayment`.`borrow_id` = `gfb_borrow`.`id`" +
                " and `type` = 4) and `repay_at_yes` >=  '" + DateHelper.dateToString(startDate) + "'  " +
                "and `repay_at_yes` <=  '" + DateHelper.dateToString(endDate) + "'  limit 1");
        resultMap = jdbcTemplate.queryForMap(sql.toString());
        if (!CollectionUtils.isEmpty(resultMap)) {
            qdSumRepay = NumberHelper.toLong(resultMap.get("qdSumRepay"));
            qdSumRepayPrincipal = NumberHelper.toLong(resultMap.get("qdSumRepayPrincipal"));
        }

        Long tjSumRepayment = 0l;
        Long tjSumRepaymentPrincipal = 0l;
        Long qdSumRepayment = 0l;
        Long qdSumRepaymentPrincipal = 0l;
        Long jzSumRepayment = 0l;
        Long jzSumRepaymentPrincipal = 0l;
        Statistic statistic = statisticService.findLast();
        if (ObjectUtils.isEmpty(statistic)) {
            tjSumRepayment = statistic.getTjWaitRepayTotal();
            tjSumRepaymentPrincipal = statistic.getTjWaitRepayPrincipalTotal();
            qdSumRepayment = statistic.getQdWaitRepayTotal();
            qdSumRepaymentPrincipal = statistic.getQdWaitRepayPrincipalTotal();
            jzSumRepayment = statistic.getJzWaitRepayTotal();
            jzSumRepaymentPrincipal = statistic.getJzWaitRepayPrincipalTotal();
        }

        IncrStatistic incrStatistic = incrStatisticService.findOneByDate(startDate);
        incrStatistic.setUseMoneySum(userMoneySum);
        incrStatistic.setNoUseMoneySum(noUseMoneySum.intValue());
        incrStatistic.setTjSumPublish(tjSumPublish.intValue());
        incrStatistic.setJzSumPublish(jzSumPublish.intValue());
        incrStatistic.setLzSumPublish(lzSumPublish.intValue());
        incrStatistic.setQdSumPublish(qdSumPublish.intValue());
        incrStatistic.setMiaoSumPublish(miaoSumPublish.intValue());
        incrStatistic.setTjSumSuccess(tjSumSuccess.intValue());
        incrStatistic.setJzSumSuccess(jzSumSuccess.intValue());
        incrStatistic.setLzSumSuccess(lzSumSuccess.intValue());
        incrStatistic.setMiaoSumSuccess(miaoSumSuccess.intValue());
        incrStatistic.setQdSumSuccess(qdSumSuccess.intValue());
        incrStatistic.setTjSumRepay(tjSumRepay.intValue());
        incrStatistic.setTjSumRepayPrincipal(tjSumRepayPrincipal.intValue());
        incrStatistic.setJzSumRepay(jzSumRepay.intValue());
        incrStatistic.setJzSumRepayPrincipal(jzSumRepayPrincipal.intValue());
        incrStatistic.setQdSumRepay(qdSumRepay.intValue());
        incrStatistic.setQdSumRepayPrincipal(qdSumRepayPrincipal.intValue());
        incrStatistic.setTjSumRepayment(tjSumRepayment);
        incrStatistic.setTjSumRepaymentPrincipal(tjSumRepaymentPrincipal);
        incrStatistic.setJzSumRepayment(jzSumRepayment);
        incrStatistic.setJzSumRepaymentPrincipal(jzSumRepaymentPrincipal);
        incrStatistic.setQdSumRepayment(qdSumRepayment);
        incrStatistic.setQdSumRepaymentPrincipal(qdSumRepaymentPrincipal);
        incrStatisticService.save(incrStatistic);
    }
}
