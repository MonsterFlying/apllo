package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.MultiCaculateHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.system.biz.StatisticBiz;
import com.gofobao.framework.system.entity.Statistic;
import com.gofobao.framework.system.service.StatisticService;
import com.gofobao.framework.system.vo.response.IndexStatistics;
import com.gofobao.framework.system.vo.response.VoViewIndexStatisticsWarpRes;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * Created by Max on 17/6/2.
 */
@Component
@Slf4j
public class StatisticBizImpl implements StatisticBiz {
    @Autowired
    StatisticService statisticService;

    @Autowired
    private RedisHelper redisHelper;

    private static final Gson GSON = new Gson();

    @Override
    @Transactional
    public boolean caculate(Statistic changeEntity) throws Exception {
        Preconditions.checkNotNull(changeEntity, "StatisticBizImpl.caculate: changeEntity is empty");
        log.info(String.format("全站统计增加: %s", GSON.toJson(changeEntity)));
        Statistic statistic = statisticService.findLast();
        Preconditions.checkNotNull(statistic, "StatisticBizImpl.caculate: statistic is empty");
        MultiCaculateHelper.caculate(Statistic.class, statistic, changeEntity);
        statistic.setUpdatedAt(new Date());
        statisticService.save(statistic);
        return true;
    }

    /**
     * 首页查询
     * @return
     */
    @Override
    public ResponseEntity<VoViewIndexStatisticsWarpRes> query() {
        IndexStatistics indexStatistics = new IndexStatistics();
        Gson gson = new Gson();
        try {
            String redisStr = redisHelper.get("indexStatistic", null);
            if (!StringUtils.isEmpty(redisStr)) {
                indexStatistics = gson.fromJson(redisStr, IndexStatistics.class);
            } else {
                Statistic statistic = statisticService.findLast();
                Long borrowTotal = statistic.getBorrowTotal();
                indexStatistics.setTransactionsTotal(StringHelper.formatMon(borrowTotal / 100d));
                indexStatistics.setDueTotal(StringHelper.formatMon(statistic.getWaitRepayTotal() / 100d));
                redisHelper.put("indexStatistic",gson.toJson(indexStatistics));
            }
        } catch (Exception e) {
        }
        VoViewIndexStatisticsWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewIndexStatisticsWarpRes.class);
        warpRes.setIndexStatistics(indexStatistics);
        return ResponseEntity.ok(warpRes);
    }
}
