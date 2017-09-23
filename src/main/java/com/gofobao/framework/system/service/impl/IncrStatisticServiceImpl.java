package com.gofobao.framework.system.service.impl;

import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.ExceptionEmailHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.member.repository.UsersRepository;
import com.gofobao.framework.system.entity.IncrStatistic;
import com.gofobao.framework.system.entity.Statistic;
import com.gofobao.framework.system.repository.IncrStatisticRepository;
import com.gofobao.framework.system.service.IncrStatisticService;
import com.gofobao.framework.system.service.StatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Max on 17/6/2.
 */
@Service
@Slf4j
public class IncrStatisticServiceImpl implements IncrStatisticService {

    @Autowired
    IncrStatisticRepository incrStatisticRepository;

    @Autowired
    private IncrStatisticService incrStatisticService;

    @Autowired
    private StatisticService statisticService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ExceptionEmailHelper exceptionEmailHelper;

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    public IncrStatistic findOneByDate(Date data) {

        Date begin = DateHelper.beginOfDate(data);
        Date end = DateHelper.endOfDate(data);
        Pageable pageable = new PageRequest(0, 1, new Sort(new Sort.Order(Sort.Direction.DESC, "id")));
        Page<IncrStatistic> all = incrStatisticRepository.findAll(pageable);

        IncrStatistic incrStatistic = null;
        if (all.getTotalElements() == 0) { // 第一次常见
            incrStatistic = new IncrStatistic();
            incrStatistic.setDate(data);
            incrStatistic = incrStatisticRepository.save(incrStatistic);
        } else {
            IncrStatistic oldIncrStatistic = all.getContent().get(0);
            Date oldDate = oldIncrStatistic.getDate();
            boolean state = DateHelper.isBetween(oldDate, begin, end);
            if (state) {
                return oldIncrStatistic;
            } else {
                IncrStatistic newIncrStatistic = new IncrStatistic();
                newIncrStatistic.setRegisterTotalCount(oldIncrStatistic.getRegisterTotalCount() == null ? 0 : oldIncrStatistic.getRegisterTotalCount());  // 注册总数
                newIncrStatistic.setRealRegisterTotalCount(oldIncrStatistic.getRealRegisterTotalCount() == null ? 0 : oldIncrStatistic.getRealRegisterTotalCount());  // 实名注册总数
                newIncrStatistic.setTenderJzTotalCount(oldIncrStatistic.getTenderJzTotalCount() == null ? 0 : oldIncrStatistic.getTenderJzTotalCount());
                newIncrStatistic.setTenderLzTotalCount(oldIncrStatistic.getTenderLzTotalCount() == null ? 0 : oldIncrStatistic.getTenderLzTotalCount());
                newIncrStatistic.setTenderMiaoTotalCount(oldIncrStatistic.getTenderMiaoTotalCount() == null ? 0 : oldIncrStatistic.getTenderMiaoTotalCount());
                newIncrStatistic.setTenderQdTotalCount(oldIncrStatistic.getTenderQdTotalCount() == null ? 0 : oldIncrStatistic.getTenderQdTotalCount());
                newIncrStatistic.setTenderTjTotalCount(oldIncrStatistic.getTenderTjTotalCount() == null ? 0 : oldIncrStatistic.getTenderTjTotalCount());
                newIncrStatistic.setTenderTotal(oldIncrStatistic.getTenderTotal() == null ? 0 : oldIncrStatistic.getTenderTotal());
                newIncrStatistic.setDate(data);
                incrStatistic = incrStatisticRepository.save(newIncrStatistic);
            }
        }
        return incrStatistic;
    }

    @Override
    public IncrStatistic save(IncrStatistic dbIncrStatistic) {
        return incrStatisticRepository.save(dbIncrStatistic);
    }

    @Autowired
    private UsersRepository usersRepository;

    /**
     * 注册人数统计
     *
     * @return
     */
    @Override
    public Long registerTotal() {
        return  usersRepository.registerUserCount();
    }
    /**
     * 每日统计调度
     *
     * @param date
     */
    @Override
    public void dayStatistic(Date date) {
        try {
            log.info("每日统计任务调度启动!");
            Date startDate = DateHelper.beginOfDate(DateHelper.subDays(date, 1));
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
            Long cashSum = 0L;
            sql = new StringBuffer("SELECT SUM(money) AS cashSum FROM gfb_cash_log WHERE  (status=1 or status=4 or status=0) AND created_at>='" + DateHelper.dateToString(startDate) + "' AND created_at<='" + DateHelper.dateToString(endDate) + "' ");
            resultMap = jdbcTemplate.queryForMap(sql.toString());
            if (!CollectionUtils.isEmpty(resultMap)) {
                cashSum = NumberHelper.toLong(resultMap.get("cashSum"));
            }
            Long tjSumRepayment = 0l;
            Long tjSumRepaymentPrincipal = 0l;
            Long qdSumRepayment = 0l;
            Long qdSumRepaymentPrincipal = 0l;
            Long jzSumRepayment = 0l;
            Long jzSumRepaymentPrincipal = 0l;
            Statistic statistic = statisticService.findLast();
            if (!ObjectUtils.isEmpty(statistic)) {
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
            incrStatistic.setCashSum(cashSum);
            incrStatisticService.save(incrStatistic);
        } catch (Exception e) {
            exceptionEmailHelper.sendException("每日统计调度-失败", e);
        }
    }
}
