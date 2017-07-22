package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.MultiCaculateHelper;
import com.gofobao.framework.helper.RedisHelper;
import com.gofobao.framework.system.biz.StatisticBiz;
import com.gofobao.framework.system.contants.DictAliasCodeContants;
import com.gofobao.framework.system.entity.DictItem;
import com.gofobao.framework.system.entity.DictValue;
import com.gofobao.framework.system.entity.Statistic;
import com.gofobao.framework.system.service.DictItemService;
import com.gofobao.framework.system.service.DictValueService;
import com.gofobao.framework.system.service.IncrStatisticService;
import com.gofobao.framework.system.service.StatisticService;
import com.gofobao.framework.system.vo.response.IndexStatistics;
import com.gofobao.framework.system.vo.response.VoViewIndexStatisticsWarpRes;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private TenderService tenderService;
    @Autowired
    private DictValueService dictValueService;

    @Autowired
    private DictItemService dictItemService;

    @Autowired
    private IncrStatisticService incrStatisticService;

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
     *
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
                indexStatistics.setTransactionsTotal(borrowTotal);
                indexStatistics.setDueTotal(statistic.getWaitRepayTotal());
                indexStatistics.setBorrowTotal(statistic.getBorrowItems());
                indexStatistics.setEarnings(statistic.getUserIncomeTotal());
                indexStatistics.setYesterdayDueTotal(0);
                //注册人数
                BigDecimal registerTotal = incrStatisticService.registerTotal();
                indexStatistics.setRegisterTotal(registerTotal);
                //起头金额&年华利率

                DictItem dictItem= dictItemService.findTopByAliasCodeAndDel(DictAliasCodeContants.INDEX_CONFIG,0);
                List<DictValue> dictValue=dictValueService.findByItemId(dictItem.getId());
                Map<String,String> dictValueMap=dictValue.stream().collect(Collectors.toMap(DictValue::getValue02, DictValue::getValue01));
                indexStatistics.setApr(Integer.valueOf(dictValueMap.get("annualized").toString()));
                indexStatistics.setStartMoney(Integer.valueOf(dictValueMap.get("startMoney").toString()));

                Map<String, Integer> tenderStatistic = tenderService.statistic();
                //昨日成交
                indexStatistics.setYesterdayDueTotal(tenderStatistic.get("yesterdayTender"));
                //今日成功
                indexStatistics.setTodayDueTotal(tenderStatistic.get("todayTender"));
                redisHelper.put("indexStatistic", gson.toJson(indexStatistics));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        VoViewIndexStatisticsWarpRes warpRes = VoBaseResp.ok("查询成功", VoViewIndexStatisticsWarpRes.class);
        warpRes.setIndexStatistics(indexStatistics);
        return ResponseEntity.ok(warpRes);
    }
}
