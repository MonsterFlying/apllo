package com.gofobao.framework.scheduler;


import com.github.wenhao.jpa.Specifications;
import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.api.model.voucher_pay_cancel.VoucherPayCancelRequest;
import com.gofobao.framework.api.model.voucher_pay_cancel.VoucherPayCancelResponse;
import com.gofobao.framework.asset.entity.NewAssetLog;
import com.gofobao.framework.asset.service.NewAssetLogService;
import com.gofobao.framework.award.biz.RedPackageBiz;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.core.vo.VoBaseResp;
import com.gofobao.framework.financial.biz.FinancialSchedulerBiz;
import com.gofobao.framework.financial.entity.FinancialScheduler;
import com.gofobao.framework.financial.service.FinancialSchedulerService;
import com.gofobao.framework.helper.*;
import com.gofobao.framework.member.entity.BrokerBouns;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.BrokerBounsService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserBonusScheduler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BrokerBounsService brokerBounsService;

    @Autowired
    private AssetChangeProvider assetChangeProvider;

    @Autowired
    private UserThirdAccountService userThirdAccountService;

    @Autowired
    private JixinManager jixinManager;

    @Autowired
    private FinancialSchedulerBiz financialSchedulerBiz;

    @Autowired
    private NewAssetLogService newAssetLogService;

    @Autowired
    private RedPackageBiz redPackageBiz;

    /**
     * 理财师提成
     */
    @Scheduled(cron = "0 35 23 * * ? ")
    public void brokerProcess() {
        Date nowDate = new Date();
        log.info(String.format("理财师调度启动: %s", DateHelper.dateToString(nowDate, DateHelper.DATE_FORMAT_YMD_NUM)));
        // 执行调度
        Date validDate = DateHelper.createDate(2016, 8, 14, 0, 0, 0);
        validDate = DateHelper.max(DateHelper.subYears(DateHelper.beginOfDate(new Date()), 1), validDate);
        int pageIndex = 1;
        int pageSize = 50;
        int level = 0;
        double awardApr = 0d;
        Integer bounsAward = 0;
        List<Map<String, Object>> resultList = null;
        do {
            StringBuffer sqlStr = new StringBuffer(" SELECT sum(t4.tj_wait_collection_principal+t4.qd_wait_collection_principal)AS wait_principal_total, " +
                    " t1.id AS user_id,t2.tj_wait_collection_principal,t2.qd_wait_collection_principal FROM gfb_users t1 " +
                    " INNER JOIN gfb_user_cache t2 ON t1.id=t2.user_id INNER JOIN gfb_users t3 ON t1.id=t3.parent_id INNER JOIN gfb_user_cache t4 ON t3.id=t4.user_id " +
                    " WHERE t2.tj_wait_collection_principal+t2.qd_wait_collection_principal>=1000000 AND t3.created_at>='" + DateHelper.dateToString(validDate) + "' AND t3.source IN(0,1,2,9) " +
                    " AND NOT EXISTS(SELECT 1 FROM gfb_ticheng_user t5 WHERE t5.user_id=t1.id AND t5.type=0)GROUP BY t1.id HAVING wait_principal_total>=73000");

            String limitSql = " limit " + pageSize + " offset " + (pageIndex - 1) * pageSize;
            resultList = jdbcTemplate.queryForList(sqlStr.append(limitSql).toString());
            if (CollectionUtils.isEmpty(resultList)) {
                return;
            }

            for (Map<String, Object> map : resultList) {
                level = 1;
                awardApr = 0.002;
                if ((NumberHelper.toInt(map.get("tj_wait_collection_principal"))
                        + NumberHelper.toInt(map.get("qd_wait_collection_principal"))) >= 50000000
                        || NumberHelper.toInt(map.get("wait_principal_total")) >= 80000000) {
                    level = 3;
                    awardApr = 0.005;
                } else if ((NumberHelper.toInt(map.get("tj_wait_collection_principal"))
                        + NumberHelper.toInt(map.get("qd_wait_collection_principal"))) >= 10000000
                        || NumberHelper.toInt(map.get("wait_principal_total")) >= 20000000) {
                    level = 2;
                    awardApr = 0.003;
                }

                bounsAward = NumberHelper.toInt(NumberHelper.toInt(map.get("wait_principal_total")) * awardApr / 365);  // 派发金额
                if (bounsAward <= 1) {
                    continue;
                }
                String remark = String.format("接收理财师提成奖励 %s元", StringHelper.formatDouble(bounsAward.longValue() / 100D, true));
                long userId = NumberHelper.toLong(map.get("user_id"));  // 接收红包账户ID
                String seqlNo = String.format("%s_lcs_%s", DateHelper.dateToString(nowDate, DateHelper.DATE_FORMAT_YMD_NUM), userId);  // 辨别当前是否重复派发的标志
                try {
                    log.info(String.format("理财师红包派发: %s", userId));
                    boolean result = redPackageBiz.commonPublishRedpack(userId, bounsAward, AssetChangeTypeEnum.receiveCommissions, seqlNo, remark, userId);
                    if (result) {
                        BrokerBouns brokerBouns = new BrokerBouns();
                        brokerBouns.setUserId((long) NumberHelper.toInt(map.get("user_id")));
                        brokerBouns.setLevel(level);
                        brokerBouns.setCreatedAt(new Date());
                        brokerBouns.setAwardApr((int) MathHelper.myRound(awardApr * 10000, 0));
                        brokerBouns.setAwardApr(new Double(MoneyHelper.round(awardApr * 100, 0)).intValue());
                        brokerBouns.setWaitPrincipalTotal(NumberHelper.toLong(map.get("wait_principal_total")));
                        brokerBouns.setBounsAward(new Double(MoneyHelper.round(bounsAward, 0)).intValue());
                        brokerBounsService.save(brokerBouns);
                    }
                } catch (Exception e) {
                    log.error("理财师红包派发失败", e);
                }
            }
            pageIndex++;
        } while (resultList.size() >= 50);
    }

    /**
     * 天提成
     */
    @Scheduled(cron = "0 30 23 * * ? ")
    public void dayProcess() {
        Date nowDate = new Date();
        log.info("每日天提成调度启动");
        //查询当前调度是否执行成功过
        int pageIndex = 1;
        int pageSize = 50;
        long money = 0;
        List<Map<String, Object>> resultList = null;
        do {
            StringBuffer daySqlStr = new StringBuffer("select sum(gfb_asset.collection) - sum(gfb_asset.payment) as sum, `gfb_ticheng_user`.`user_id` as userId" +
                    " from `gfb_ticheng_user` inner join `gfb_users` on `gfb_ticheng_user`.`user_id` = `gfb_users`.`parent_id` inner join `gfb_asset`" +
                    " on `gfb_users`.`id` = `gfb_asset`.`user_id` where `gfb_ticheng_user`.`type` = 0 and (`gfb_ticheng_user`.`start_at` is null or " +
                    " `gfb_ticheng_user`.`start_at` < '" + DateHelper.dateToString(new Date()) + "') and (`gfb_ticheng_user`.`end_at` is null or " +
                    " `gfb_ticheng_user`.`end_at` > '" + DateHelper.dateToString(new Date()) + "') " +
                    " group by `gfb_ticheng_user`.`user_id` having `sum` >= " + Math.ceil(365 / 0.005));
            String limitSql = " limit " + pageSize + " offset " + (pageIndex - 1) * pageSize;
            resultList = jdbcTemplate.queryForList(daySqlStr.append(limitSql).toString());
            if (CollectionUtils.isEmpty(resultList)) {
                return;
            }

            for (Map<String, Object> map : resultList) {
                money = (int) MoneyHelper.round(NumberHelper.toInt(map.get("sum")) * 0.005 / 365, 0);
                Long userId = NumberHelper.toLong(map.get("userId"));
                String remark = String.format("接收每日提成奖励 %s元", StringHelper.formatDouble(money / 100D, true));
                String seqlNo = String.format("%s_mrtc_%s", DateHelper.dateToString(nowDate, DateHelper.DATE_FORMAT_YMD_NUM), userId);  // 辨别当前是否重复派发的标志
                try {
                    log.info(String.format("每日提成红包派发: %s", userId));
                    boolean result = redPackageBiz.commonPublishRedpack(userId, money, AssetChangeTypeEnum.receiveCommissions, seqlNo, remark, userId);
                    if (result) {
                        log.info("每日提成用户派发成功");
                    } else {
                        log.info("每日提成用户派发失败");
                    }
                } catch (Exception e) {
                    log.error("每日提成红包派发失败", e);
                }
            }
            pageIndex++;
        } while (resultList.size() >= 50);

    }

    /**
     * 月提成
     */
    @Scheduled(cron = "0 35 23 1 * ? ")
    @Transactional(rollbackFor = Exception.class)
    public void monthProcess() {
        Date nowDate = new Date();
        log.info("每月提成任务调度启动");
        int pageIndex = 1;
        int pageSize = 50;
        long money = 0;
        int sum = 0;
        List<Map<String, Object>> resultList = null;
        do {
            StringBuilder monthSql = new StringBuilder("select sum(gfb_asset.collection) - sum(gfb_asset.payment) as sum, " +
                    "`gfb_ticheng_user`.`user_id` as userId from `gfb_ticheng_user` inner join `gfb_users` on `gfb_ticheng_user`.`user_id`" +
                    " = `gfb_users`.`parent_id` inner join `gfb_asset` on `gfb_users`.`id` = `gfb_asset`.`user_id` where `gfb_ticheng_user`.`type` = 1 " +
                    "and (`gfb_ticheng_user`.`start_at` is null or `gfb_ticheng_user`.`start_at` < '" + DateHelper.dateToString(new Date()) + "') and " +
                    "(`gfb_ticheng_user`.`end_at` is null or `gfb_ticheng_user`.`end_at` > '" + DateHelper.dateToString(new Date()) + "') and " +
                    "`gfb_users`.`created_at` < '2016-08-14 00:00:00' group by `gfb_ticheng_user`.`user_id` having `sum` >= " + Math.ceil(1 / .0002));
            String limitSql = " limit " + pageSize + " offset " + (pageIndex - 1) * pageSize;
            resultList = jdbcTemplate.queryForList(monthSql.append(limitSql).toString());
            if (CollectionUtils.isEmpty(resultList)) {
                return;
            }

            for (Map<String, Object> map : resultList) {
                sum = NumberHelper.toInt(map.get("sum"));
                if (sum < Math.pow(10, 9)) {
                    money = (int) MoneyHelper.round(sum * 0.0002, 0);
                } else if (sum > Math.pow(10, 9) && sum <= 5 * Math.pow(10, 9)) {
                    money = 200 + (int) MoneyHelper.round((sum - Math.pow(10, 9)) * .0003, 0);
                } else if (sum > 5 * Math.pow(10, 9) && sum <= Math.pow(10, 10)) {
                    money = 1400 + (int) MoneyHelper.round((sum - 5 * Math.pow(10, 9)) * .0004, 0);
                } else {
                    money = 3400 + (int) MoneyHelper.round((sum - Math.pow(10, 10)) * .0005, 0);
                }

                Long userId = NumberHelper.toLong(map.get("userId"));
                String remark = String.format("接收每月提成奖励 %s元", StringHelper.formatDouble(money / 100D, true));
                String seqlNo = String.format("%s_mytc_%s", DateHelper.dateToString(nowDate, DateHelper.DATE_FORMAT_YMD_NUM), userId);  // 辨别当前是否重复派发的标志
                try {
                    log.info(String.format("每月提成红包派发: %s", userId));
                    boolean result = redPackageBiz.commonPublishRedpack(userId, money, AssetChangeTypeEnum.receiveCommissions, seqlNo, remark, userId);
                    if (result) {
                        log.info("每月提成用户派发成功");
                    } else {
                        log.info("每月提成用户派发失败");
                    }
                } catch (Exception e) {
                    log.error("每月提成用户派发失败", e);
                }
            }
            pageIndex++;
        } while (resultList.size() >= 50);

    }
}
