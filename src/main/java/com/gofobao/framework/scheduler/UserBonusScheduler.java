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


    /**
     * 理财师提成
     */
    @Scheduled(cron = "0 35 23 * * ? ")
    public void brokerProcess() {
        log.info("理财师调度启动");

        //记录调度日志
        FinancialScheduler financialScheduler = new FinancialScheduler();
        List<Map<String, String>> results = Lists.newArrayList();
        try {
            boolean isExecute = financialSchedulerBiz.isExecute("PUSHMONEY");
            if (isExecute) {
                log.info("理财师提成已调度");
                return;
            }
            financialScheduler.setCreateAt(new Date());
            //执行调度
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
                //查询今天已发放过的理财师红包
                Date nowDate = new Date();
                List<Long> userIds = resultList.stream().map(p -> Long.valueOf(p.get("user_id").toString())).collect(Collectors.toList());
                Specification<NewAssetLog> specification = Specifications.<NewAssetLog>and()
                        .in("userId", userIds.toArray())
                        .between("createTime", new Range(DateHelper.beginOfDate(nowDate), DateHelper.endOfDate(nowDate)))
                        .build();
                List<NewAssetLog> newAssetLogs = newAssetLogService.findAll(specification);
                Map<Long,NewAssetLog> newAssetLogMap=newAssetLogs.stream().collect(Collectors.toMap(NewAssetLog::getUserId, Function.identity()));
                for (Map<String, Object> map : resultList) {
                    level = 1;
                    awardApr = 0.002;
                    if ((NumberHelper.toInt(map.get("tj_wait_collection_principal")) + NumberHelper.toInt(map.get("qd_wait_collection_principal"))) >= 50000000 || NumberHelper.toInt(map.get("wait_principal_total")) >= 80000000) {
                        level = 3;
                        awardApr = 0.005;
                    } else if ((NumberHelper.toInt(map.get("tj_wait_collection_principal")) + NumberHelper.toInt(map.get("qd_wait_collection_principal"))) >= 10000000 || NumberHelper.toInt(map.get("wait_principal_total")) >= 20000000) {
                        level = 2;
                        awardApr = 0.003;
                    }

                    bounsAward = NumberHelper.toInt(NumberHelper.toInt(map.get("wait_principal_total")) * awardApr / 365);
                    if (bounsAward <= 1) {
                        continue;
                    }

                    long userId = NumberHelper.toLong(map.get("user_id"));
                    UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
                    if (ObjectUtils.isEmpty(userThirdAccount)) {
                        log.error("当前用户没有开户: " + userId);
                        continue;
                    }
                    String groupSeqNo = assetChangeProvider.getGroupSeqNo();
                    long redId = assetChangeProvider.getRedpackAccountId();
                    UserThirdAccount redPacketAccount = userThirdAccountService.findByUserId(redId);
                    //请求即信红包
                    Map<String, String> paramMaps = Maps.newHashMap();
                    //流水号
                    String seqNo = RandomUtil.getRandomString(6);
                    VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                    voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
                    //派发金额
                    String money = StringHelper.formatDouble(bounsAward, 100, false);
                    voucherPayRequest.setTxAmount(money);
                    //接受红包账号
                    voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
                    voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                    voucherPayRequest.setDesLine("理财师提成");
                    voucherPayRequest.setSeqNo(seqNo);
                    voucherPayRequest.setChannel(ChannelContant.HTML);
                    String orgTxDate = DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMD_NUM);
                    String orgTxTime = DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_HMS_NUM);
                    //派发时间
                    voucherPayRequest.setTxTime(orgTxTime);
                    voucherPayRequest.setTxDate(orgTxDate);
                    paramMaps.put("forAccountId", userThirdAccount.getAccountId());
                    paramMaps.put("orgTxDate", orgTxDate);
                    paramMaps.put("orgTxTime", orgTxTime);
                    paramMaps.put("money", money);
                    VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                    if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                        String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                        log.error(new Gson().toJson(response));
                        log.error("理财师调度:" + msg);
                        paramMaps.put("status", "0");
                        continue;
                    }
                    try {
                        // 发放理财师奖励
                        AssetChange redpackPublish = new AssetChange();
                        redpackPublish.setMoney(bounsAward.longValue());
                        redpackPublish.setType(AssetChangeTypeEnum.publishCommissions);  //  扣除红包
                        redpackPublish.setUserId(redId);
                        redpackPublish.setRemark(String.format("派发理财师提成奖励 %s元", StringHelper.formatDouble(bounsAward.longValue() / 100D, true)));
                        redpackPublish.setGroupSeqNo(groupSeqNo);
                        redpackPublish.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                        redpackPublish.setForUserId(userId);
                        redpackPublish.setSourceId(0L);
                        assetChangeProvider.commonAssetChange(redpackPublish);
                        // 接收理财师
                        AssetChange redpackR = new AssetChange();
                        redpackR.setMoney(bounsAward.longValue());
                        redpackR.setType(AssetChangeTypeEnum.receiveCommissions);
                        redpackR.setUserId(userId);
                        redpackR.setRemark(String.format("接收理财师提成奖励 %s元", StringHelper.formatDouble(bounsAward.longValue() / 100D, true)));
                        redpackR.setGroupSeqNo(groupSeqNo);
                        redpackR.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                        redpackR.setForUserId(redId);
                        redpackR.setSourceId(0L);
                        assetChangeProvider.commonAssetChange(redpackR);

                        BrokerBouns brokerBouns = new BrokerBouns();
                        brokerBouns.setUserId((long) NumberHelper.toInt(map.get("user_id")));
                        brokerBouns.setLevel(level);
                        brokerBouns.setCreatedAt(new Date());
                        brokerBouns.setAwardApr((int) MathHelper.myRound(awardApr * 10000, 0));
                        brokerBouns.setAwardApr(new Double(MoneyHelper.round(awardApr * 100, 0)).intValue());
                        brokerBouns.setWaitPrincipalTotal(NumberHelper.toLong(map.get("wait_principal_total")));
                        brokerBouns.setBounsAward(new Double(MoneyHelper.round(bounsAward, 0)).intValue());
                        brokerBounsService.save(brokerBouns);
                    } catch (Exception e) {

                    }
                }
                pageIndex++;
            } while (resultList.size() >= 50);
            financialScheduler.setData(DateHelper.dateToString(new Date()));
            financialScheduler.setState(1);
            financialScheduler.setResMsg("调度成功");
            financialScheduler.setUpdateAt(new Date());
        } catch (Throwable e) {
            financialScheduler.setState(0);
            financialScheduler.setResMsg("调度失败");
            financialScheduler.setUpdateAt(new Date());
            log.error("UserBonusScheduler brokerProcess error:", e);
        }
        financialScheduler.setName("理财师提成调度");
        financialScheduler.setType("PUSHMONEY");
        financialScheduler.setDoNum(1);
        financialSchedulerBiz.save(financialScheduler);
    }


    /**
     * 撤销红包
     * @param sendRedPackageList
     */
    private void revocationRedPackage(List<Map<String, String>> sendRedPackageList) {
        sendRedPackageList.forEach(p -> {
            //派发的账户
            String forAccountId = p.get("forAccountId");
            //原交易时间
            String orgTxDate = p.get("orgTxDate");
            String orgTxTime = p.get("orgTxTime");
            String bounsAward = p.get("bounsAward");
            String seqNo = p.get("seqNo");
            //撤销红包
            VoucherPayCancelRequest voucherPayCancelRequest = new VoucherPayCancelRequest();
     //       voucherPayCancelRequest.setAccountId(redPacketAccount.getAccountId());
            voucherPayCancelRequest.setTxAmount(bounsAward);
            voucherPayCancelRequest.setOrgTxDate(orgTxDate);
            voucherPayCancelRequest.setOrgTxTime(orgTxTime);
            voucherPayCancelRequest.setForAccountId(forAccountId);
            voucherPayCancelRequest.setOrgSeqNo(seqNo);
            voucherPayCancelRequest.setAcqRes(forAccountId);
            voucherPayCancelRequest.setChannel(ChannelContant.HTML);
            VoucherPayCancelResponse result = jixinManager.send(JixinTxCodeEnum.UNSEND_RED_PACKET, voucherPayCancelRequest, VoucherPayCancelResponse.class);
            if ((ObjectUtils.isEmpty(result)) || (!JixinResultContants.SUCCESS.equals(result.getRetCode()))) {
                String msg = ObjectUtils.isEmpty(result) ? "当前网络出现异常, 请稍后尝试！" : result.getRetMsg();
                log.error(msg);
            }
        });
    }


    /**
     * 天提成
     */
    @Scheduled(cron = "0 30 23 * * ? ")
    @Transactional(rollbackFor = Exception.class)
    public void dayProcess() {
        log.info("每日天提成调度启动");
        //查询当前调度是否执行成功过

        boolean isExecute = financialSchedulerBiz.isExecute("DAY_PUSHMONEY");
        if (isExecute) {
            log.info("理财师-天-提成已调度");
            return;
        }
        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setCreateAt(new Date());
        try {
            int pageIndex = 1;
            int pageSize = 50;
            long money = 0;
            List<Map<String, Object>> resultList = null;
            long redId = assetChangeProvider.getRedpackAccountId();
            UserThirdAccount redPacketAccount = userThirdAccountService.findByUserId(redId);
            String groupSeqNo = assetChangeProvider.getGroupSeqNo();
            do {
                StringBuffer daySqlStr = new StringBuffer("select sum(gfb_asset.collection) - sum(gfb_asset.payment) as sum, `gfb_ticheng_user`.`user_id` as userId" +
                        " from `gfb_ticheng_user` inner join `gfb_users` on `gfb_ticheng_user`.`user_id` = `gfb_users`.`parent_id` inner join `gfb_asset`" +
                        " on `gfb_users`.`id` = `gfb_asset`.`user_id` where `gfb_ticheng_user`.`type` = 0 and (`gfb_ticheng_user`.`start_at` is null or " +
                        " `gfb_ticheng_user`.`start_at` < '" + DateHelper.dateToString(new Date()) + "') and (`gfb_ticheng_user`.`end_at` is null or " +
                        " `gfb_ticheng_user`.`end_at` > '" + DateHelper.dateToString(new Date()) + "') " +
                        " group by `gfb_ticheng_user`.`user_id` having `sum` >= " + Math.ceil(365 / 0.005));
                String limitSql = " limit " + pageSize + " offset " + (pageIndex - 1) * pageSize;
                resultList = jdbcTemplate.queryForList(daySqlStr.append(limitSql).toString());
                financialScheduler.setData(new Gson().toJson(resultList));

                if (CollectionUtils.isEmpty(resultList)) {
                    return;
                }


                for (Map<String, Object> map : resultList) {
                    money = (int) MoneyHelper.round(NumberHelper.toInt(map.get("sum")) * 0.005 / 365, 0);
                    Long userId = NumberHelper.toLong(map.get("userId"));
                    UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
                    if (ObjectUtils.isEmpty(userThirdAccount)) {
                        log.error("每日调度用户未开户:" + userId);
                        continue;
                    }

                    log.info("每日提成:" + new Gson().toJson(userThirdAccount));
                    VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                    voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
                    voucherPayRequest.setTxAmount(StringHelper.formatDouble(money, 100, false));
                    voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
                    voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                    voucherPayRequest.setDesLine("每日提成");
                    voucherPayRequest.setChannel(ChannelContant.HTML);
                    VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                    if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                        String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                        log.error(new Gson().toJson(response));
                        log.error("每日调度:" + msg);
                        continue;
                    }

                    // 发放理财师奖励
                    AssetChange redpackPublish = new AssetChange();
                    redpackPublish.setMoney(money);
                    redpackPublish.setType(AssetChangeTypeEnum.publishCommissions);  //  扣除红包
                    redpackPublish.setUserId(redId);
                    redpackPublish.setRemark(String.format("派发每日提成奖励 %s元", StringHelper.formatDouble(money / 100D, true)));
                    redpackPublish.setGroupSeqNo(groupSeqNo);
                    redpackPublish.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                    redpackPublish.setForUserId(userId);
                    redpackPublish.setSourceId(0L);
                    assetChangeProvider.commonAssetChange(redpackPublish);

                    // 接收理财师
                    AssetChange redpackR = new AssetChange();
                    redpackR.setMoney(money);
                    redpackR.setType(AssetChangeTypeEnum.receiveCommissions);
                    redpackR.setUserId(userId);
                    redpackR.setRemark(String.format("接收每日提成奖励 %s元", StringHelper.formatDouble(money / 100D, true)));
                    redpackR.setGroupSeqNo(groupSeqNo);
                    redpackR.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                    redpackR.setForUserId(redId);
                    redpackR.setSourceId(0L);
                    assetChangeProvider.commonAssetChange(redpackR);
                }
                pageIndex++;

            } while (resultList.size() >= 50);

            financialScheduler.setState(1);
            financialScheduler.setResMsg("调度成功");
            financialScheduler.setCreateAt(new Date());
        } catch (Throwable e) {
            financialScheduler.setState(0);
            financialScheduler.setResMsg("调度失败");
            log.error("UserBonusScheduler dayProcess error:", e);
        }
        financialScheduler.setName(DateHelper.dateToString(new Date()) + "理财师每天提成");
        financialScheduler.setType("DAY_PUSHMONEY");
        financialScheduler.setDoNum(financialScheduler.getDoNum());
        financialSchedulerBiz.save(financialScheduler);
    }

    /**
     * 月提成
     */
    @Scheduled(cron = "0 35 23 1 * ? ")
    @Transactional(rollbackFor = Exception.class)
    public void monthProcess() {
        log.info("每月提成任务调度启动");
        boolean isExecute = financialSchedulerBiz.isExecute("MONTH_PUSHMONEY");
        if (isExecute) {
            log.info("理财师-月-提成已调度");
            return;
        }
        FinancialScheduler financialScheduler = new FinancialScheduler();
        financialScheduler.setCreateAt(new Date());
        try {
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
                long redId = assetChangeProvider.getRedpackAccountId();
                UserThirdAccount redPacketAccount = userThirdAccountService.findByUserId(redId);
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


                    String groupSeqNo = assetChangeProvider.getGroupSeqNo();
                    Long userId = NumberHelper.toLong(map.get("userId"));
                    UserThirdAccount userThirdAccount = userThirdAccountService.findByUserId(userId);
                    if (ObjectUtils.isEmpty(userThirdAccount)) {
                        log.error("每日调度用户未开户:" + userId);
                        continue;
                    }
                    //请求即信红包
                    VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                    voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
                    voucherPayRequest.setTxAmount(StringHelper.formatDouble(money, 100, false));
                    voucherPayRequest.setForAccountId(userThirdAccount.getAccountId());
                    voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                    voucherPayRequest.setDesLine("每月提成");
                    voucherPayRequest.setChannel(ChannelContant.HTML);
                    VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                    if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                        String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                        log.error(new Gson().toJson(response));
                        log.error("每月提成调度:" + msg);
                        continue;
                    }

                    // 发放理财师奖励
                    AssetChange redpackPublish = new AssetChange();
                    redpackPublish.setMoney(money);
                    redpackPublish.setType(AssetChangeTypeEnum.publishCommissions);  //  扣除红包
                    redpackPublish.setUserId(redId);
                    redpackPublish.setRemark(String.format("派发每月提成奖励 %s元", StringHelper.formatDouble(money / 100D, true)));
                    redpackPublish.setGroupSeqNo(groupSeqNo);
                    redpackPublish.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                    redpackPublish.setForUserId(userId);
                    redpackPublish.setSourceId(0L);
                    assetChangeProvider.commonAssetChange(redpackPublish);

                    // 接收理财师
                    AssetChange redpackR = new AssetChange();
                    redpackR.setMoney(money);
                    redpackR.setType(AssetChangeTypeEnum.receiveCommissions);
                    redpackR.setUserId(userId);
                    redpackR.setRemark(String.format("接收每月提成奖励 %s元", StringHelper.formatDouble(money / 100D, true)));
                    redpackR.setGroupSeqNo(groupSeqNo);
                    redpackR.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                    redpackR.setForUserId(redId);
                    redpackR.setSourceId(0L);
                    assetChangeProvider.commonAssetChange(redpackR);
                }
                pageIndex++;
            } while (resultList.size() >= 50);
            financialScheduler.setState(1);
            financialScheduler.setResMsg("调度成功");
            financialScheduler.setCreateAt(new Date());
            financialScheduler.setData(new Gson().toJson(resultList));
        } catch (Throwable e) {
            financialScheduler.setState(0);
            financialScheduler.setResMsg("调度失败");
            log.error("UserBonusScheduler monthProcess error:", e);
        }
        financialScheduler.setName(DateHelper.dateToString(new Date()) + "理财师每月提成");
        financialScheduler.setType("MONTH_PUSHMONEY");
        financialScheduler.setDoNum(financialScheduler.getDoNum());
        financialSchedulerBiz.save(financialScheduler);

    }
}
