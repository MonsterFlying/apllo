package com.gofobao.framework.scheduler;

import com.gofobao.framework.common.capital.CapitalChangeEntity;
import com.gofobao.framework.common.capital.CapitalChangeEnum;
import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.MathHelper;
import com.gofobao.framework.helper.NumberHelper;
import com.gofobao.framework.helper.project.CapitalChangeHelper;
import com.gofobao.framework.member.entity.BrokerBouns;
import com.gofobao.framework.member.service.BrokerBounsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Zeke on 2017/7/4.
 */
@Component
@Slf4j
public class UserBonusScheduler {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CapitalChangeHelper capitalChangeHelper;
    @Autowired
    private BrokerBounsService brokerBounsService;

    /**
     * 理财师提成
     */
    //@Scheduled(fixedRate = 20 * 60 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public void brokerProcess() {
        try {
            StringBuffer sql = new StringBuffer("SELECT sum(t4.tj_wait_collection_principal+t4.qd_wait_collection_principal)AS wait_principal_total," +
                    "t1.id AS user_id,t2.tj_wait_collection_principal,t2.qd_wait_collection_principal FROM gfb_users t1#提成用户" +
                    "INNER JOIN gfb_user_cache t2 ON t1.id=t2.user_id INNER JOIN gfb_users t3 ON t1.id=t3.parent_id INNER JOIN gfb_user_cache t4 ON t3.id=t4.user_id " +
                    "WHERE t2.tj_wait_collection_principal+t2.qd_wait_collection_principal>=1000000 AND t3.created_at>='$validDate'AND t3.source IN(0,1,2,9)" +
                    "AND NOT EXISTS(SELECT 1 FROM gfb_ticheng_user t5 WHERE t5.user_id=t1.id AND t5.type=0)GROUP BY t1.id HAVING wait_principal_total>=73000");
            int pageIndex = 1;
            int pageSize = 50;
            int money = 0;
            int level = 0;
            double awardApr = 0;
            double bounsAward = 0;
            CapitalChangeEntity entity = null;
            List<Map<String, Object>> resultList = null;
            do {
                sql.append(" limit " + (pageIndex - 1) * pageSize + "," + pageIndex * pageSize);
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

                    entity = new CapitalChangeEntity();
                    entity.setUserId(NumberHelper.toInt(map.get("user_id")));
                    entity.setMoney((int) bounsAward);
                    entity.setType(CapitalChangeEnum.Bonus);
                    entity.setRemark("提成");
                    capitalChangeHelper.capitalChange(entity);


                    BrokerBouns brokerBouns = new BrokerBouns();
                    brokerBouns.setUserId((long) NumberHelper.toInt(map.get("user_id")));
                    brokerBouns.setLevel(level);
                    brokerBouns.setAwardApr((int) MathHelper.myRound(awardApr * 100, 0));
                    brokerBouns.setWaitPrincipalTotal(NumberHelper.toLong(map.get("wait_principal_total")));
                    brokerBouns.setBounsAward((int) MathHelper.myRound(bounsAward, 0));
                    brokerBounsService.save(brokerBouns);
                }
            } while (resultList.size() >= 50);
        } catch (Exception e) {
            log.error("UserBonusScheduler brokerProcess error:", e);
        }
    }

    /**
     * 天提成
     */
    public void dayProcess() {
        try {
            StringBuffer sql = new StringBuffer("select sum(gfb_asset.collection) - sum(gfb_asset.payment) as sum, `gfb_ticheng_user`.`user_id` as userId" +
                    " from `gfb_ticheng_user` inner join `gfb_users` on `gfb_ticheng_user`.`user_id` = `gfb_users`.`parent_id` inner join `gfb_asset`" +
                    " on `gfb_users`.`id` = `gfb_asset`.`user_id` where `gfb_ticheng_user`.`type` = ? and (`gfb_ticheng_user`.`start_at` is null or " +
                    "`gfb_ticheng_user`.`start_at` < " + DateHelper.dateToString(new Date()) + ") and (`gfb_ticheng_user`.`end_at` is null or `gfb_ticheng_user`.`end_at` > " + DateHelper.dateToString(new Date()) + ") " +
                    "group by `gfb_ticheng_user`.`user_id` having `sum` >= " + Math.ceil(365 / 0.005));
            int pageIndex = 1;
            int pageSize = 50;
            int money = 0;
            CapitalChangeEntity entity = null;
            List<Map<String, Object>> resultList = null;
            do {
                sql.append(" limit " + (pageIndex - 1) * pageSize + "," + pageIndex * pageSize);
                resultList = jdbcTemplate.queryForList(sql.toString());
                for (Map<String, Object> map : resultList) {
                    money = (int) MathHelper.myRound(NumberHelper.toInt(map.get("sum")) / 100 * 0.005 / 365, 0);
                    entity = new CapitalChangeEntity();
                    entity.setUserId(NumberHelper.toInt(map.get("userId")));
                    entity.setMoney(money);
                    entity.setType(CapitalChangeEnum.Bonus);
                    entity.setRemark("提成");
                    capitalChangeHelper.capitalChange(entity);
                }
            } while (resultList.size() >= 50);
        } catch (Exception e) {
            log.error("UserBonusScheduler dayProcess error:", e);
        }
    }

    /**
     * 月提成
     */
    public void monthProcess() {
        try {
            StringBuffer sql = new StringBuffer("select sum(gfb_asset.collection) - sum(gfb_asset.payment) as sum, " +
                    "`gfb_ticheng_user`.`user_id` as userId from `gfb_ticheng_user` inner join `gfb_users` on `gfb_ticheng_user`.`user_id`" +
                    " = `gfb_users`.`parent_id` inner join `gfb_asset` on `gfb_users`.`id` = `gfb_asset`.`user_id` where `gfb_ticheng_user`.`type` = 1 " +
                    "and (`gfb_ticheng_user`.`start_at` is null or `gfb_ticheng_user`.`start_at` < " + DateHelper.dateToString(new Date()) + ") and " +
                    "(`gfb_ticheng_user`.`end_at` is null or `gfb_ticheng_user`.`end_at` > " + DateHelper.dateToString(new Date()) + ") and " +
                    "`gfb_users`.`created_at` < 2016-08-14 00:00:00 group by `gfb_ticheng_user`.`user_id` having `sum` >= " + Math.ceil(1 / .0002));
            int pageIndex = 1;
            int pageSize = 50;
            int money = 0;
            int sum = 0;
            CapitalChangeEntity entity = null;
            List<Map<String, Object>> resultList = null;
            do {
                sql.append(" limit " + (pageIndex - 1) * pageSize + "," + pageIndex * pageSize);
                resultList = jdbcTemplate.queryForList(sql.toString());
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
                    entity = new CapitalChangeEntity();
                    entity.setUserId(NumberHelper.toInt(map.get("userId")));
                    entity.setMoney(money);
                    entity.setRemark("提成");
                    entity.setType(CapitalChangeEnum.Bonus);
                    capitalChangeHelper.capitalChange(entity);
                }
            } while (resultList.size() >= 50);
        } catch (Exception e) {
            log.error("UserBonusScheduler monthProcess error:", e);
        }
    }
}
