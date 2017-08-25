package com.gofobao.framework.system.biz.impl;

import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MathHelper;
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
import com.gofobao.framework.system.vo.response.NewIndexStatisics;
import com.gofobao.framework.system.vo.response.OperateDataStatistics;
import com.gofobao.framework.system.vo.response.VoViewIndexStatisticsWarpRes;
import com.gofobao.framework.tender.service.TenderService;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

    static final Date startDate = DateHelper.stringToDate("2013-7-23 00:00:00", DateHelper.DATE_FORMAT_YMDHMS);
    LoadingCache<String, NewIndexStatisics> artclesCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, NewIndexStatisics>() {
                @Override
                public NewIndexStatisics load(String type) throws Exception {
                    NewIndexStatisics newIndexStatisics = new NewIndexStatisics();
                    Statistic statistic = statisticService.findLast();

                    Long borrowTotal = statistic.getBorrowTotal();  // 交易总额
                    // Long waitRepayTotal = statistic.getWaitRepayTotal(); // 待收
                    Calendar currCalender = Calendar.getInstance();
                    Calendar startCalender = Calendar.getInstance();
                    startCalender.setTime(startDate);

                    // 计算运营天数
                    int years = 0;
                    while (startCalender.before(currCalender)) {
                        startCalender.add(Calendar.YEAR, 1);
                        if (startCalender.before(currCalender)) {
                            years++;
                        }
                    }

                    Date currYearDate = DateHelper.addYears(startDate, years);
                    int days = DateHelper.diffInDays(currCalender.getTime(), currYearDate, false);
                    newIndexStatisics.setSafeOperation(String.format("%s年%s天", years, days));
                    //注册人数
                    BigDecimal registerTotal = incrStatisticService.registerTotal();
                    long register = registerTotal.longValue();
                    String titel = formatNumber(borrowTotal / 100);
                    if (titel.contains("亿")) {
                        titel = titel.substring(0, titel.indexOf("亿") + 1);
                    } else if (titel.contains("万")) {
                        titel = titel.substring(0, titel.indexOf("万") + 1);
                    }
                    newIndexStatisics.setRegsiterCount(formatNumber(register));
                    newIndexStatisics.setTotalTransaction(String.format("%s元", titel)); // 交易总额
                    return newIndexStatisics;
                }
            });

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
     * 格式化时间
     *
     * @param num
     * @return
     */
    public static String formatNumber(Long num) {
        long[] limits = {MathHelper.pow(10, 8), MathHelper.pow(10, 4), 1};
        String[] units = {"亿", "万", ""};

        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0, len = limits.length; i < len; i++) {
            long limitNum = limits[i];
            if (num / limitNum >= 1) {
                stringBuffer.append(num / limitNum).append(units[i]);
                num = num % limitNum;
            }

        }
        return stringBuffer.toString();
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
                indexStatistics.setYesterdayDueTotal(0l);
                //注册人数
                BigDecimal registerTotal = incrStatisticService.registerTotal();
                indexStatistics.setRegisterTotal(registerTotal);
                //起头金额&年华利率
                DictItem dictItem = dictItemService.findTopByAliasCodeAndDel(DictAliasCodeContants.INDEX_CONFIG, 0);
                List<DictValue> dictValue = dictValueService.findByItemId(dictItem.getId());
                Map<String, String> dictValueMap = dictValue.stream().collect(Collectors.toMap(DictValue::getValue02, DictValue::getValue01));
                indexStatistics.setApr(Integer.valueOf(dictValueMap.get("annualized").toString()));
                indexStatistics.setStartMoney(Integer.valueOf(dictValueMap.get("startMoney").toString()));

                Map<String, Long> tenderStatistic = tenderService.statistic();
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

    @Override
    public NewIndexStatisics queryMobileIndexData() {
        try {
            return artclesCache.get("mobile");
        } catch (ExecutionException e) {
            return new NewIndexStatisics();
        }
    }


    LoadingCache<String, OperateDataStatistics> operateData = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .maximumSize(1024)
            .build(new CacheLoader<String, OperateDataStatistics>() {
                @Override
                public OperateDataStatistics load(String type) throws Exception {
                    OperateDataStatistics dataStatistics = VoBaseResp.ok("查询成功", OperateDataStatistics.class);
                    ResponseEntity<VoViewIndexStatisticsWarpRes> responseEntity = query();
                    VoViewIndexStatisticsWarpRes statisticsWarpRes = responseEntity.getBody();
                    IndexStatistics indexStatistics = statisticsWarpRes.getIndexStatistics();
                    //年化率
                    dataStatistics.setApr(indexStatistics.getApr());
                    //待收总额
                    dataStatistics.setDueTotal(indexStatistics.getDueTotal());
                    //注册人数
                    dataStatistics.setRegisterTotal(indexStatistics.getRegisterTotal());
                    //为用户赚取收益
                    dataStatistics.setEarnings(indexStatistics.getEarnings());
                    //累计成交笔数
                    dataStatistics.setBorrowTotal(indexStatistics.getBorrowTotal());
                    //交易总额
                    dataStatistics.setTransactionsTotal(indexStatistics.getTransactionsTotal());
                /*    //已还本息
                    dataStatistics.setSettleCapitalTotal(indexStatistics.);*/

/*
                    //平均每笔投资金额
                    dataStatistics.setAverageTenderMoney();
                    //人均累计投资金额
                    dataStatistics.setAverageTenderMoneySum();
                    //累计投资人数
                    dataStatistics.setTenderNoOfPeople();*/

                    return dataStatistics;
                }
            });

    @Override
    public ResponseEntity<OperateDataStatistics> queryOperateData() {

        try {
            OperateDataStatistics dataStatistics=operateData.get("");
            return ResponseEntity.ok(dataStatistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(VoBaseResp.error(VoBaseResp.ERROR, "查询异常", OperateDataStatistics.class));
        }
    }
}
