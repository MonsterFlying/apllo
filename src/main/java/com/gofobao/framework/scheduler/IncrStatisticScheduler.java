package com.gofobao.framework.scheduler;

import com.gofobao.framework.helper.DateHelper;
import com.gofobao.framework.helper.NumberHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Zeke on 2017/7/10.
 */
public class IncrStatisticScheduler {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 30 0 * * ? ")
    public void process(){
        Date startDate = DateHelper.beginOfDate(DateHelper.subDays(new Date(), 1));
        Date endDate = DateHelper.endOfDate(startDate);

        StringBuffer sql = new StringBuffer("select sum(gfb_daily_asset.use_money) as useMoneySum, sum(gfb_daily_asset.no_use_money) as noUseMoneySum " +
                "from `gfb_users` left join `gfb_daily_asset` on `gfb_users`.`id` = `gfb_daily_asset`.`user_id`" +
                " where `gfb_users`.`id` not in (22) " +
                "and `gfb_users`.`type` <> 'borrower' " +
                "and `gfb_daily_asset`.`date` = '"+DateHelper.dateToString(startDate,DateHelper.DATE_FORMAT_YMD)+"' " +
                "limit 1");
        List<Map<String,Object>> resultList = jdbcTemplate.queryForList(sql.toString());
        if (CollectionUtils.isEmpty(resultList)){
            return;
        }
        Map<String,Object> result = resultList.get(0);
        int userMoneySum = NumberHelper.toInt(result.get("useMoneySum"));
        int noUseMoneySum = NumberHelper.toInt(result.get("noUseMoneySum"));

        sql = new StringBuffer("select sum(`money`) as aggregate from `gfb_borrow` " +
                "where `type` = 0 and `tender_id` is null and `release_at` >= '"+DateHelper.dateToString(startDate)+"'" +
                " and `release_at` <= '"+DateHelper.dateToString(endDate)+"'");

    }
}
