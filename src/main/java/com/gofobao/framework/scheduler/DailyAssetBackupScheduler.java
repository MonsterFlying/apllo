package com.gofobao.framework.scheduler;

import com.gofobao.framework.helper.DateHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by Zeke on 2017/7/4.
 */
@Component
@Slf4j
public class DailyAssetBackupScheduler {

    @Autowired
    JdbcTemplate jdbcTemplate;

    //@Scheduled(fixedRate = 20 * 60 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public void process() {
        try {
            String yesterdayDate = DateHelper.dateToString(DateHelper.subDays(new Date(), 1), DateHelper.DATE_FORMAT_YMD);
            StringBuffer sql = new StringBuffer("delete from daily_asset WHERE `date` =  ")
                    .append("\"")
                    .append(yesterdayDate)
                    .append("\"");
            jdbcTemplate.update(sql.toString());
            sql = new StringBuffer("$sql = 'INSERT INTO daily_asset ( user_id, use_money, no_use_money, virtual_money, collection, payment, date, updated_at ) " +
                    "SELECT user_id, use_money, no_use_money, virtual_money, collection, payment, \'").append(yesterdayDate)
                    .append("\', updated_at FROM asset");
            jdbcTemplate.update(sql.toString());
            sql = new StringBuffer("UPDATE daily_asset AS t1, (SELECT * FROM yesterday_asset) AS t2 SET t1.use_money = t2.use_money, " +
                    "t1.no_use_money = t2.no_use_money, t1.virtual_money = t2.virtual_money, t1.collection = t2.collection, t1.payment = t2.payment, t1.updated_at = t2.updated_at " +
                    "WHERE t1.user_id = t2.user_id AND t1.updated_at >= \'").append(DateHelper.dateToString(new Date(), DateHelper.DATE_FORMAT_YMDHMS)).append("\'");
            jdbcTemplate.update(sql.toString());
        } catch (DataAccessException e) {
            log.error("DailyAssetBackup error:", e);
        }
    }
}
