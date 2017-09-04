package com.gofobao.framework.scheduler;


import com.gofobao.framework.api.contants.ChannelContant;
import com.gofobao.framework.api.contants.DesLineFlagContant;
import com.gofobao.framework.api.contants.JixinResultContants;
import com.gofobao.framework.api.helper.JixinManager;
import com.gofobao.framework.api.helper.JixinTxCodeEnum;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayRequest;
import com.gofobao.framework.api.model.voucher_pay.VoucherPayResponse;
import com.gofobao.framework.common.assets.AssetChange;
import com.gofobao.framework.common.assets.AssetChangeProvider;
import com.gofobao.framework.common.assets.AssetChangeTypeEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.StringHelper;
import com.gofobao.framework.member.entity.BrokerBouns;
import com.gofobao.framework.member.entity.UserThirdAccount;
import com.gofobao.framework.member.service.BrokerBounsService;
import com.gofobao.framework.member.service.UserThirdAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    /**
     * 理财师提成
     */
    @Transactional(rollbackFor = Exception.class)
    //@Scheduled(cron = "0 1 0 * * ? ")
    public void brokerProcess() {
        log.info("理财师调度启动");
        try {
            Date validDate = DateHelper.createDate(2016, 8, 14, 0, 0, 0);
            validDate = DateHelper.max(DateHelper.subYears(DateHelper.beginOfDate(new Date()), 1), validDate);


            StringBuffer sql = new StringBuffer(" SELECT sum(t4.tj_wait_collection_principal+t4.qd_wait_collection_principal)AS wait_principal_total, " +
                    " t1.id AS user_id,t2.tj_wait_collection_principal,t2.qd_wait_collection_principal FROM gfb_users t1 " +
                    " INNER JOIN gfb_user_cache t2 ON t1.id=t2.user_id INNER JOIN gfb_users t3 ON t1.id=t3.parent_id INNER JOIN gfb_user_cache t4 ON t3.id=t4.user_id " +
                    " WHERE t2.tj_wait_collection_principal+t2.qd_wait_collection_principal>=1000000 AND t3.created_at>='" + DateHelper.dateToString(validDate) + "' AND t3.source IN(0,1,2,9) " +
                    " AND NOT EXISTS(SELECT 1 FROM gfb_ticheng_user t5 WHERE t5.user_id=t1.id AND t5.type=0)GROUP BY t1.id HAVING wait_principal_total>=73000");
            int pageIndex = 1;
            int pageSize = 50;
            int level = 0;
            double awardApr = 0;
            Double bounsAward = 0D;
            List<Map<String, Object>> resultList = null;
            do {
                sql.append(" limit " + (pageIndex++ - 1) * pageSize + "," + pageIndex * pageSize);
                resultList = jdbcTemplate.queryForList(sql.toString());
                for (Map<String, Object> map : resultList) {
                    level = 1;
                    awardApr = 0.002;
                    if ((NumberHelper.toInt(map.get("tj_wait_collection_principal")) + NumberHelper.toInt(map.get("qd_wait_collection_principal"))) >= 50000000 || NumberHelper.toInt(map.get("wait_principal_total")) >= 80000000) {
                        level = 3;
                        awardApr = .005;
                    } else if ((NumberHelper.toInt(map.get("tj_wait_collection_principal")) + NumberHelper.toInt(map.get("qd_wait_collection_principal"))) >= 10000000 || NumberHelper.toInt(map.get("wait_principal_total")) >= 20000000) {
                        level = 2;
                        awardApr = .003;
                    }

                    bounsAward = MathHelper.myRound(NumberHelper.toInt(map.get("wait_principal_total")) * .01 * awardApr / 365, 2);

                    if (bounsAward <= .01) {
                        continue;
                    }

                    long userId = NumberHelper.toLong(map.get("user_id"));
                    String groupSeqNo = assetChangeProvider.getGroupSeqNo();
                    long redId = assetChangeProvider.getRedpackAccountId();
                    UserThirdAccount redPacketAccount = userThirdAccountService.findByUserId(redId);
                    //请求即信红包
                    VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                    voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
                    voucherPayRequest.setTxAmount(StringHelper.formatDouble(bounsAward, 100, false));
                    voucherPayRequest.setForAccountId(String.valueOf(redId));
                    voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                    voucherPayRequest.setDesLine("理财师提成");
                    voucherPayRequest.setChannel(ChannelContant.HTML);
                    VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                    if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                        String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                        log.error("理财师调度:" + msg);

                    }


                    // 发放理财师奖励
                    AssetChange redpackPublish = new AssetChange();
                    redpackPublish.setMoney(bounsAward.longValue());
                    redpackPublish.setType(AssetChangeTypeEnum.publishCommissions);  //  扣除红包
                    redpackPublish.setUserId(redId);
                    redpackPublish.setForUserId(userId);
                    redpackPublish.setRemark(String.format("派发理财师提成奖励 %s元", StringHelper.formatDouble(bounsAward.longValue() / 100D, true)));
                    redpackPublish.setGroupSeqNo(groupSeqNo);
                    redpackPublish.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                    redpackPublish.setForUserId(redId);
                    redpackPublish.setSourceId(0L);
                    assetChangeProvider.commonAssetChange(redpackPublish);

                    // 接收理财师
                    AssetChange redpackR = new AssetChange();
                    redpackR.setMoney(bounsAward.longValue());
                    redpackR.setType(AssetChangeTypeEnum.receiveCommissions);
                    redpackR.setUserId(userId);
                    redpackR.setForUserId(redId);
                    redpackR.setRemark(String.format("接收理财师提成奖励 %s元", StringHelper.formatDouble(bounsAward.longValue() / 100D, true)));
                    redpackR.setGroupSeqNo(groupSeqNo);
                    redpackR.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                    redpackR.setForUserId(redId);
                    redpackR.setSourceId(0L);
                    assetChangeProvider.commonAssetChange(redpackR);

                    BrokerBouns brokerBouns = new BrokerBouns();
                    brokerBouns.setUserId((long) NumberHelper.toInt(map.get("user_id")));
                    brokerBouns.setLevel(level);
                    brokerBouns.setAwardApr((int) MathHelper.myRound(awardApr * 100, 0));
                    brokerBouns.setWaitPrincipalTotal(NumberHelper.toLong(map.get("wait_principal_total")));
                    brokerBouns.setBounsAward((int) MathHelper.myRound(bounsAward, 0));
                    brokerBounsService.save(brokerBouns);
                }
            } while (resultList.size() >= 50);
        } catch (Throwable e) {
            log.error("UserBonusScheduler brokerProcess error:", e);
        }
    }

    /**
     * 天提成
     */
   // @Scheduled(cron = "0 30 23 * * ? ")
    public void dayProcess() {
        log.info("每日天提成调度启动");
        try {
            StringBuffer sql = new StringBuffer("select sum(gfb_asset.collection) - sum(gfb_asset.payment) as sum, `gfb_ticheng_user`.`user_id` as userId" +
                    " from `gfb_ticheng_user` inner join `gfb_users` on `gfb_ticheng_user`.`user_id` = `gfb_users`.`parent_id` inner join `gfb_asset`" +
                    " on `gfb_users`.`id` = `gfb_asset`.`user_id` where `gfb_ticheng_user`.`type` = 0 and (`gfb_ticheng_user`.`start_at` is null or " +
                    " `gfb_ticheng_user`.`start_at` < '" + DateHelper.dateToString(new Date()) + "') and (`gfb_ticheng_user`.`end_at` is null or " +
                    " `gfb_ticheng_user`.`end_at` > '" + DateHelper.dateToString(new Date()) + "') " +
                    " group by `gfb_ticheng_user`.`user_id` having `sum` >= " + Math.ceil(365 / 0.005));
            int pageIndex = 1;
            int pageSize = 50;
            long money = 0;
            List<Map<String, Object>> resultList = null;
            do {
                sql.append(" limit " + (pageIndex++ - 1) * pageSize + "," + pageIndex * pageSize);
                resultList = jdbcTemplate.queryForList(sql.toString());
                long redId = assetChangeProvider.getRedpackAccountId();
                UserThirdAccount redPacketAccount = userThirdAccountService.findByUserId(redId);
                for (Map<String, Object> map : resultList) {
                    money = (int) MathHelper.myRound(NumberHelper.toInt(map.get("sum")) / 100 * 0.005 / 365, 0);
                    Long userId = NumberHelper.toLong(map.get("userId"));
                    String groupSeqNo = assetChangeProvider.getGroupSeqNo();

                    //请求即信红包
                    VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                    voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
                    voucherPayRequest.setTxAmount(StringHelper.formatDouble(money, 100, false));
                    voucherPayRequest.setForAccountId(String.valueOf(redId));
                    voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                    voucherPayRequest.setDesLine("每日提成");
                    voucherPayRequest.setChannel(ChannelContant.HTML);
                    VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                    if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                        String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                        log.error("每日调度:" + msg);

                    }


                    // 发放理财师奖励
                    AssetChange redpackPublish = new AssetChange();
                    redpackPublish.setMoney(money);
                    redpackPublish.setType(AssetChangeTypeEnum.publishCommissions);  //  扣除红包
                    redpackPublish.setUserId(redId);
                    redpackPublish.setForUserId(userId);
                    redpackPublish.setRemark(String.format("派发每日提成奖励 %s元", StringHelper.formatDouble(money / 100D, true)));
                    redpackPublish.setGroupSeqNo(groupSeqNo);
                    redpackPublish.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                    redpackPublish.setForUserId(redId);
                    redpackPublish.setSourceId(0L);
                    assetChangeProvider.commonAssetChange(redpackPublish);

                    // 接收理财师
                    AssetChange redpackR = new AssetChange();
                    redpackR.setMoney(money);
                    redpackR.setType(AssetChangeTypeEnum.receiveCommissions);
                    redpackR.setUserId(userId);
                    redpackR.setForUserId(redId);
                    redpackR.setRemark(String.format("接收每日提成奖励 %s元", StringHelper.formatDouble(money / 100D, true)));
                    redpackR.setGroupSeqNo(groupSeqNo);
                    redpackR.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                    redpackR.setForUserId(redId);
                    redpackR.setSourceId(0L);
                    assetChangeProvider.commonAssetChange(redpackR);
                }
            } while (resultList.size() >= 50);
        } catch (Throwable e) {
            log.error("UserBonusScheduler dayProcess error:", e);
        }
    }

    /**
     * 月提成
     */
    //@Scheduled(cron = "0 35 23 1 * ? ")
    public void monthProcess() {
        log.info("每月提成任务调度启动");
        try {
            StringBuffer sql = new StringBuffer("select sum(gfb_asset.collection) - sum(gfb_asset.payment) as sum, " +
                    "`gfb_ticheng_user`.`user_id` as userId from `gfb_ticheng_user` inner join `gfb_users` on `gfb_ticheng_user`.`user_id`" +
                    " = `gfb_users`.`parent_id` inner join `gfb_asset` on `gfb_users`.`id` = `gfb_asset`.`user_id` where `gfb_ticheng_user`.`type` = 1 " +
                    "and (`gfb_ticheng_user`.`start_at` is null or `gfb_ticheng_user`.`start_at` < '" + DateHelper.dateToString(new Date()) + "') and " +
                    "(`gfb_ticheng_user`.`end_at` is null or `gfb_ticheng_user`.`end_at` > '" + DateHelper.dateToString(new Date()) + "') and " +
                    "`gfb_users`.`created_at` < '2016-08-14 00:00:00' group by `gfb_ticheng_user`.`user_id` having `sum` >= " + Math.ceil(1 / .0002));
            int pageIndex = 1;
            int pageSize = 50;
            long money = 0;
            int sum = 0;
            List<Map<String, Object>> resultList = null;
            do {
                sql.append(" limit " + (pageIndex++ - 1) * pageSize + "," + pageIndex * pageSize);
                resultList = jdbcTemplate.queryForList(sql.toString());
                long redId = assetChangeProvider.getRedpackAccountId();
                UserThirdAccount redPacketAccount = userThirdAccountService.findByUserId(redId);
                for (Map<String, Object> map : resultList) {
                    sum = NumberHelper.toInt(map.get("sum"));
                    if (sum < Math.pow(10, 9)) {
                        money = (int) MathHelper.myRound(sum / 100 * 0.0002, 0);
                    } else if (sum > Math.pow(10, 9) && sum <= 5 * Math.pow(10, 9)) {
                        money = 200 + (int) MathHelper.myRound((sum - Math.pow(10, 9)) / 100 * .0003, 0);
                    } else if (sum > 5 * Math.pow(10, 9) && sum <= Math.pow(10, 10)) {
                        money = 1400 + (int) MathHelper.myRound((sum - 5 * Math.pow(10, 9)) / 100 * .0004, 0);
                    } else {
                        money = 3400 + (int) MathHelper.myRound((sum - Math.pow(10, 10)) / 100 * .0005, 0);
                    }


                    String groupSeqNo = assetChangeProvider.getGroupSeqNo();
                    Long userId = NumberHelper.toLong(map.get("userId"));
                    //请求即信红包
                    VoucherPayRequest voucherPayRequest = new VoucherPayRequest();
                    voucherPayRequest.setAccountId(redPacketAccount.getAccountId());
                    voucherPayRequest.setTxAmount(StringHelper.formatDouble(money, 100, false));
                    voucherPayRequest.setForAccountId(String.valueOf(redId));
                    voucherPayRequest.setDesLineFlag(DesLineFlagContant.TURE);
                    voucherPayRequest.setDesLine("每月提成");
                    voucherPayRequest.setChannel(ChannelContant.HTML);
                    VoucherPayResponse response = jixinManager.send(JixinTxCodeEnum.SEND_RED_PACKET, voucherPayRequest, VoucherPayResponse.class);
                    if ((ObjectUtils.isEmpty(response)) || (!JixinResultContants.SUCCESS.equals(response.getRetCode()))) {
                        String msg = ObjectUtils.isEmpty(response) ? "当前网络不稳定，请稍候重试" : response.getRetMsg();
                        log.error("每月提成调度:" + msg);
                    }


                    // 发放理财师奖励
                    AssetChange redpackPublish = new AssetChange();
                    redpackPublish.setMoney(money);
                    redpackPublish.setType(AssetChangeTypeEnum.publishCommissions);  //  扣除红包
                    redpackPublish.setUserId(redId);
                    redpackPublish.setForUserId(userId);
                    redpackPublish.setRemark(String.format("派发每月提成奖励 %s元", StringHelper.formatDouble(money / 100D, true)));
                    redpackPublish.setGroupSeqNo(groupSeqNo);
                    redpackPublish.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                    redpackPublish.setForUserId(redId);
                    redpackPublish.setSourceId(0L);
                    assetChangeProvider.commonAssetChange(redpackPublish);

                    // 接收理财师
                    AssetChange redpackR = new AssetChange();
                    redpackR.setMoney(money);
                    redpackR.setType(AssetChangeTypeEnum.receiveCommissions);
                    redpackR.setUserId(userId);
                    redpackR.setForUserId(redId);
                    redpackR.setRemark(String.format("接收每月提成奖励 %s元", StringHelper.formatDouble(money / 100D, true)));
                    redpackR.setGroupSeqNo(groupSeqNo);
                    redpackR.setSeqNo(String.format("%s%s%s", response.getTxDate(), response.getTxTime(), response.getSeqNo()));
                    redpackR.setForUserId(redId);
                    redpackR.setSourceId(0L);
                    assetChangeProvider.commonAssetChange(redpackR);
                }
            } while (resultList.size() >= 50);
        } catch (Throwable e) {
            log.error("UserBonusScheduler monthProcess error:", e);
        }
    }
}
